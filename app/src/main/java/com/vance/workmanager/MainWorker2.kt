package com.vance.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * 数据 互相传递
 * 后台任务
 */
class MainWorker2(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object { const val TAG = " vance" }

    // 后台任务 并且 异步的 （原理：线程池执行Runnable）
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result { // 开始执行了 ENQUEUED
        Log.d(MainWorker2.TAG, "MainWorker2 doWork: 后台任务执行了")

        // 接收 MainActivity传递过来的数据
        val dataString = workerParams.inputData.getString(" vance")
        Log.d(MainWorker2.TAG, "MainWorker2 doWork: 接收MainActivity传递过来的数据:$dataString")

        // 正在执行中 RUNNING

        // 反馈数据 给 MainActivity
        // 把任务中的数据回传到MainActivity中
        val outputData = Data.Builder().putString(" vance", "三分归元气").build()

        // return new Result.Failure(); // 本地执行 doWork 任务时 失败
        // return new Result.Retry(); // 本地执行 doWork 任务时 重试一次
        // return new Result.Success(); // 本地执行 doWork 任务时 成功 执行任务完毕
        return Result.Success(outputData) // if (workInfo.state.isFinished) Success
    }
}