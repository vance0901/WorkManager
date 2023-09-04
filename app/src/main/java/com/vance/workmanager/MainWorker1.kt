package com.vance.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

// 最简单的 执行任务
class MainWorker1(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object { const val TAG = " vance" }

    // 后台任务 并且 异步的 （原理：线程池执行Runnable）
    override fun doWork(): Result {
        Log.d(TAG, "MainWorker1 doWork: run started ... ")

        try {
            Thread.sleep(8000) // 睡眠
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Result.failure(); // 本次任务失败
        } finally {
            Log.d(TAG, "MainWorker1 doWork: run end ... ")
        }

        return Result.success(); // 本次任务成功
    }
}