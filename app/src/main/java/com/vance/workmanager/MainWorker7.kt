package com.vance.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * （你怎么知道，他被杀掉后，还在后台执行？）写入文件的方式
 * 后台任务7
 */
class MainWorker7(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    // 三个静态常量 SP的标记
    companion object {

        const val TAG = " vance"

        const val SP_NAME = "spNAME" // SP name

        const val SP_KEY = "spKEY" // SP Key

    }

    // 后台任务
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        Log.d(TAG, "MainWorker7 doWork: 后台任务执行了 started")

        // 睡眠八秒钟
        try {
            Thread.sleep(8000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // 获取SP
        val sp = applicationContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

        // 获取 sp 里面的值
        var spIntValue = sp.getInt(SP_KEY, 0)

        sp.edit().putInt(SP_KEY, ++spIntValue).apply() // 每隔8秒钟 更新文件  0 1 2 3

        Log.d(TAG, "MainWorker7 doWork: 后台任务执行了 end")

        return Result.Success() // 本地执行 doWork 任务时 成功 执行任务完毕
    }

}