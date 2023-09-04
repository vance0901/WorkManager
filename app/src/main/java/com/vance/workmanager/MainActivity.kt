package com.vance.workmanager

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() , SharedPreferences.OnSharedPreferenceChangeListener {

    private var bt6 : Button ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt6 = findViewById(R.id.bt6)
        // 绑定 SP 变化监听
        val sp = getSharedPreferences(MainWorker7.SP_NAME, MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        updateToUI() // 第一次初始一把
    }

    /**
     * 最简单的 执行任务
     * 测试后台任务 1
     *
     * @param view
     */
    fun testBackgroundWork1(view: View) {
        // OneTimeWorkRequest  单个 一次的

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(MainWorker7::class.java).build()

        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
    }

    /**
     * 数据 互相传递
     * 测试后台任务 2
     *
     * @param view
     */
    fun testBackgroundWork2(view: View?) {
        // 单一的任务  一次
        val oneTimeWorkRequest1: OneTimeWorkRequest

        // 数据
        val sendData = Data.Builder().putString(" vance", "九阳神功").build()

        // 请求对象初始化
        oneTimeWorkRequest1 = OneTimeWorkRequest.Builder(MainWorker2::class.java)
            .setInputData(sendData) // 数据的携带  发送  一般都是 携带到Request里面去 发送数据给 WorkManager2
            .build()

        // 一般都是通过 状态机 接收 WorkManager2的回馈数据
        // 状态机（LiveData） 才能接收 WorkManager回馈的数据
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest1.id)
            .observe(this, { workInfo ->

                // ENQUEUED,RUNNING,SUCCEEED
                Log.d(MainWorker2.TAG, "状态：" + workInfo.state.name)

                // ENQUEUED, RUNNING  都取不到 回馈的数据 都是 null
                // Log.d(MainWorker2.TAG, "取到了任务回传的数据: " + workInfo.outputData.getString(" vance"))

                if (workInfo.state.isFinished) { // 判断成功 SUCCEEDED状态
                    Log.d(MainWorker2.TAG, "取到了任务回传的数据: " + workInfo.outputData.getString(" vance"))
                }
            })

        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest1)
    }

    /**
     * 多个任务 顺序执行
     * 测试后台任务 3
     *
     * @param view
     */
    fun testBackgroundWork3(view: View) {
        // 单一的任务  一次
        val oneTimeWorkRequest3 = OneTimeWorkRequest.Builder(MainWorker3::class.java).build()
        val oneTimeWorkRequest4 = OneTimeWorkRequest.Builder(MainWorker4::class.java).build()
        val oneTimeWorkRequest5 = OneTimeWorkRequest.Builder(MainWorker5::class.java).build()
        val oneTimeWorkRequest6 = OneTimeWorkRequest.Builder(MainWorker6::class.java).build()

        // 顺序执行 3  4  5  6
        WorkManager.getInstance(this)
            .beginWith(oneTimeWorkRequest3) // 做初始化检查的任务 成功后
            .then(oneTimeWorkRequest4) // 业务1 任务 成功后
            .then(oneTimeWorkRequest5)  // 业务2 任务 成功后
            .then(oneTimeWorkRequest6) // 最后检查工作任务
            .enqueue()

        // 需求：先执行  3  4    最后执行 6
        val oneTimeWorkRequests: MutableList<OneTimeWorkRequest> = ArrayList() // 集合方式
        oneTimeWorkRequests.add(oneTimeWorkRequest3) // 先同步日志信息
        oneTimeWorkRequests.add(oneTimeWorkRequest4) // 先更新服务器数据信息

        WorkManager.getInstance(this).beginWith(oneTimeWorkRequests)
            .then(oneTimeWorkRequest6) // 最后再 检查同步
            .enqueue()
    }

    // 前面的单个任务  ENQUEEN  RUN  SUCCESS

    /**
     * 重复执行后台任务  非单个任务，多个任务
     * 测试后台任务 4
     *
     * @param view
     */
    fun testBackgroundWork4(view: View) {
        // OneTimeWorkRequest 单个  前面的三个例子  不会轮询 执行一次就OK

        // 重复的任务  多次/循环/轮询  , 哪怕设置为 10秒 轮询一次,   那么最少轮询/循环一次 15分钟（Google规定的）
        // 不能小于15分钟，否则默认修改成15分钟
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(MainWorker3::class.java, 10, TimeUnit.SECONDS)
            .build()

        // 【状态机】  为什么一直都是 ENQUEUE，因为 你是轮询的任务，所以你看不到 SUCCESS        [如果你是单个任务，就会看到SUCCESS结束任务]
        // 监听状态
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this, { workInfo ->
                Log.d(MainWorker2.TAG, "状态：" + workInfo.state.name) // ENQUEEN   RUNN  循环反复
                if (workInfo.state.isFinished) {
                    Log.d(MainWorker2.TAG, "状态：isFinished=true  注意：后台任务已经完成了...")
                }
            })
        WorkManager.getInstance(this).enqueue(periodicWorkRequest)

        // 取消 任务的执行
        // WorkManager.getInstance(this).cancelWorkById(periodicWorkRequest.getId());
    }

    /**
     * 约束条件，约束后台任务执行
     * 测试后台任务 5
     *
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun testBackgroundWork5(view: View?) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 必须是联网中
            /*.setRequiresCharging(true) // 必须是充电中
            .setRequiresDeviceIdle(true) // 必须是空闲时（例如：你没有玩游戏  例如：你有没有看大片 1亿像素的）*/
            .build()

        /**
         * 除了上面设置的约束外，WorkManger还提供了以下的约束作为Work执行的条件：
         * setRequiredNetworkType：网络连接设置
         * setRequiresBatteryNotLow：是否为低电量时运行 默认false
         * setRequiresCharging：是否要插入设备（接入电源），默认false
         * setRequiresDeviceIdle：设备是否为空闲，默认false
         * setRequiresStorageNotLow：设备可用存储是否不低于临界阈值
         */

        // 请求对象
        val request = OneTimeWorkRequest.Builder(MainWorker3::class.java)
            .setConstraints(constraints) // Request 关联  约束条件
            .build()

        // 加入队列
        WorkManager.getInstance(this).enqueue(request)
    }


    /**
     * （你怎么知道，他被杀掉后，还在后台执行？）写入文件的方式（SP），向 证明  vance说的 所言非虚
     * 测试后台任务 6
     *
     * @param view
     */
    fun testBackgroundWork6(view: View?) {
        // 约束条件
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 约束条件，必须是网络连接
            .build()

        // 构建Request
        val request = OneTimeWorkRequest.Builder(MainWorker7::class.java)
            .setConstraints(constraints)
            .build()

        // 加入队列
        WorkManager.getInstance(this).enqueue(request)
    }

    // 从SP里面获取值，显示到界面给用户看就行了
    private fun updateToUI() {
        val sp = getSharedPreferences(MainWorker7.SP_NAME, MODE_PRIVATE)
        val resultValue = sp.getInt(MainWorker7.SP_KEY, 0)
        bt6 ?.setText("测试后台任务六 -- $resultValue")
    }

    // SP归零
    fun spReset(view: View?) {
        val sp = getSharedPreferences(MainWorker7.SP_NAME, MODE_PRIVATE)
        sp.edit().putInt(MainWorker7.SP_KEY, 0).apply()
        updateToUI()
    }

    // 文件内容只要变，此函数执行
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)= updateToUI()




    // -----------------------------------------------------

    // TODO --------------------------------------------------- 下面是源码分析环节

    /**
     * TODO 分析源码
     * 测试后台任务 6
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun codeStudy(view: View?) {

        // 没有约束

        // 请求对象
        val request = OneTimeWorkRequest.Builder(MainWorker3::class.java).build()

        // 第一次 ContentProvider
        /**
         * APK清单文件里面（第一次）执行  面试官
         *  成果WorkManagerImpl构建出来了
         * 1.初始化 数据库 ROOM 来保存你的任务 （持久性保存的） 手机重启，APP被杀掉 没关系 一定执行
         * 2.初始化 埋下伏笔 new GreedyScheduler(context, taskExecutor, this)
         * 3.初始化 配置信息 configuration （执行信息，线程池任务）
         */

        WorkManager.getInstance(this) // 这里已经是第二次初始化了

             // 执行流程源码分析
            .enqueue(request)

    }
}