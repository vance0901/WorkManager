package com.vance.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * 后台任务3
 */
class MainWorker3 (context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object { const val TAG = " vance" }

    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        Log.d(TAG, "MainWorker3 doWork: 后台任务执行了")

        return Result.Success() // 本地执行 doWork 任务时 成功 执行任务完毕
    }
}