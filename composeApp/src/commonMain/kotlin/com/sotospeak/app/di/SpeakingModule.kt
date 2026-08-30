package com.sotospeak.app.di

import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.app.viewmodel.LibraryViewModel
import com.sotospeak.app.viewmodel.MySubmissionsViewModel
import com.sotospeak.app.viewmodel.PracticeViewModel
import com.sotospeak.app.viewmodel.QuestionsViewModel
import com.sotospeak.app.viewmodel.TopicsViewModel
import com.sotospeak.app.viewmodel.TrainingViewModel
import com.sotospeak.app.viewmodel.VideoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Speaking-тренажёр (спека Part 2 §8.1): записи, репозиторий, VM флоу Library → … → MySubmissions. */
val speakingModule = module {
    single<RecordingFileStorage> { RecordingFileStorage() }
    single { RecordingStore(get(), get()) }
    // Единая точка доступа speaking-VM к сети и метаданным записей (bd FunnyEnglish-5tf.5)
    single { SpeakingRepository(get(), get()) }
    factory { com.sotospeak.shared.platform.AudioPlayer() }   // прослушивание записей
    viewModel { LibraryViewModel(get()) }            // repository (сеть + прогресс тем, DC-2)
    viewModel { TopicsViewModel(get(), get()) }      // repository + Settings
    viewModel { QuestionsViewModel(get()) }
    viewModel { VideoViewModel(get(), get()) }       // repository + Settings (topic_watched_*)
    viewModel { TrainingViewModel(get(), get()) }    // repository + AudioPlayer
    viewModel { PracticeViewModel(get(), get(), get()) } // + RecordingFileStorage + TokenProvider
    viewModel { MySubmissionsViewModel(get(), get(), get()) } // repository + fileStorage + AudioPlayer
}
