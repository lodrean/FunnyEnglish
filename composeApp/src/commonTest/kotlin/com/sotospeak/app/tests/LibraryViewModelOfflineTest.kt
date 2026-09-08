package com.sotospeak.app.tests

import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.storage.OfflineCache
import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.app.viewmodel.LibraryAction
import com.sotospeak.app.viewmodel.LibraryViewModel
import com.sotospeak.shared.api.SpeakingApi
import com.sotospeak.shared.contracts.SpeakingLibrary
import com.sotospeak.shared.contracts.SpeakingSubmission
import com.sotospeak.shared.contracts.SpeakingTopicDetail
import com.sotospeak.shared.contracts.SpeakingTopicListItem
import com.sotospeak.shared.platform.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Офлайн-фолбэк библиотек (bd FunnyEnglish-h3l.8): при сетевой ошибке VM
 * показывает кэш последнего успешного ответа (OfflineCache в Settings)
 * и ставит флаг offlineData; без кэша — обычная ошибка.
 * FakeSpeakingApi вместо mockk: в commonTest мок-библиотек нет.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelOfflineTest {

    private val dispatcher = StandardTestDispatcher()
    private val settings = Settings("test_offline_${Clock.System.now().toEpochMilliseconds()}")

    private class FakeSpeakingApi : SpeakingApi {
        var fail: Boolean = true

        override suspend fun getSpeakingLibraries(): Result<List<SpeakingLibrary>> =
            if (fail) Result.failure(IllegalStateException("network down"))
            else Result.success(listOf(SpeakingLibrary("lib-2", "Свежая", null, null, 1)))

        override suspend fun getSpeakingTopics(libraryId: String): Result<List<SpeakingTopicListItem>> =
            Result.success(emptyList())

        override suspend fun getSpeakingTopicDetail(topicId: String): Result<SpeakingTopicDetail> =
            Result.failure(IllegalStateException("not used"))

        override suspend fun submitSpeakingPractice(
            topicId: String,
            durationSec: Int,
            audioBytes: ByteArray,
            fileName: String
        ): Result<SpeakingSubmission> = Result.failure(IllegalStateException("not used"))

        override suspend fun getMySpeakingSubmissions(): Result<List<SpeakingSubmission>> =
            Result.success(emptyList())

        override suspend fun getTextResource(url: String): Result<String> =
            Result.failure(IllegalStateException("not used"))
    }

    private val api = FakeSpeakingApi()
    private val cache = OfflineCache(settings)
    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = SpeakingRepository(api, RecordingStore(settings, RecordingFileStorage()))
        viewModel = LibraryViewModel(repository, cache)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun primeCache() {
        cache.save(
            OfflineCache.KEY_LIBRARIES,
            listOf(SpeakingLibrary("lib-1", "Кэшированная", null, null, 1)),
            kotlinx.serialization.builtins.ListSerializer(SpeakingLibrary.serializer())
        )
    }

    private fun refresh() {
        viewModel.onAction(LibraryAction.OnRefresh)
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun successSavesCacheAndClearsOfflineFlag() {
        primeCache()
        api.fail = false

        refresh()

        assertEquals("Свежая", viewModel.state.value.libraries.first().title)
        assertFalse(viewModel.state.value.offlineData)
    }

    @Test
    fun networkFailureFallsBackToCache() {
        primeCache()
        api.fail = true

        refresh()

        val state = viewModel.state.value
        assertTrue(state.offlineData, "должен включиться офлайн-режим")
        assertEquals("Кэшированная", state.libraries.first().title)
        assertEquals(null, state.error)
    }

    @Test
    fun networkFailureWithoutCacheShowsError() {
        api.fail = true

        refresh()

        val state = viewModel.state.value
        assertFalse(state.offlineData)
        assertTrue(state.libraries.isEmpty())
        assertTrue(state.error != null)
    }
}
