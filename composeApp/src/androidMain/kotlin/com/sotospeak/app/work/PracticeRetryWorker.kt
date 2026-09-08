package com.sotospeak.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sotospeak.app.data.PracticeRetryService
import org.koin.core.context.GlobalContext
import java.io.IOException

/**
 * Периодический фоновый retry неотправленных Practice-записей
 * (bd FunnyEnglish-h3l.19, спека §6.4). Koin стартует в App-композируемом
 * коде — в worker-only процессе он может быть не запущен → retry.
 */
class PracticeRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val koin = GlobalContext.getOrNull() ?: return Result.retry()
        val retryService = try {
            koin.get<PracticeRetryService>()
        } catch (e: Exception) {
            return Result.retry()
        }
        return try {
            retryService.retryAll()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "practice_retry"
    }
}
