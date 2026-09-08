package com.sotospeak.app.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sotospeak.app.data.PracticeRetryService
import java.util.concurrent.TimeUnit

/**
 * Регистрация фонового retry (bd FunnyEnglish-h3l.19): раз в сутки при наличии
 * сети — все pending Practice-записи уходят на backend без открытия приложения.
 * Дефолтная Worker-фабрика создаёт [PracticeRetryWorker] (ctor Context+Params).
 */
object PracticeRetryScheduling {

    fun register(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PracticeRetryWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PracticeRetryWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        )
    }
}
