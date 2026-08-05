package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.sotospeak.app.player.VideoPlayerController
import com.sotospeak.app.screens.VideoScreen
import com.sotospeak.app.viewmodel.VideoState
import com.sotospeak.designsystem.theme.FunnyTheme
import com.sotospeak.shared.model.SpeakingTopicDetail
import com.sotospeak.shared.model.SpeakingVideo
import kotlin.test.Test

/**
 * UI тесты экрана видео — DC-5 (мокап frame-video):
 * кастомные контролы (big_play_button, video_control_bar, vc_*), hint, CTA.
 * Desktop-стаб VideoPlayerController — error="unsupported", но state.videoError=false,
 * поэтому контролы отрисовываются поверх стаба.
 */
@OptIn(ExperimentalTestApi::class)
class VideoScreenTest : BaseUiTest() {

    @Test
    fun dc5MockupControlsAreVisible() = runTest(
        content = { VideoScreenForTest() }
    ) {
        onNodeWithTag("video_screen").assertIsDisplayed()
        // V1: big-play + control-bar + элементы
        onNodeWithTag("big_play_button", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("video_control_bar", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("vc_play_pause", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("vc_seek", useUnmergedTree = true).assertExists()
        onNodeWithTag("vc_time", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("vc_cc", useUnmergedTree = true).assertIsDisplayed()
        // V5: CTA
        onNodeWithTag("go_to_questions_button", useUnmergedTree = true).assertIsDisplayed()
        // V4: подсказка мокапа
        onNodeWithText(
            "Смотреть всё видео необязательно — к вопросам можно перейти в любой момент",
            substring = true
        ).assertExists()
    }

    @Test
    fun dc5ErrorStateHidesControls() = runTest(
        content = { VideoScreenForTest(state = mockVideoState(videoError = true)) }
    ) {
        onNodeWithTag("video_error", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("video_control_bar", useUnmergedTree = true).assertDoesNotExist()
    }
}

private fun mockVideoState(videoError: Boolean = false) = VideoState(
    topic = SpeakingTopicDetail(
        id = "topic-1",
        libraryId = "lib-1",
        title = "At the airport",
        video = SpeakingVideo(
            videoUrl = "http://localhost/video.mp4",
            subtitleUrl = "http://localhost/subs.vtt",
            durationSeconds = 95
        ),
        questions = emptyList()
    ),
    subtitlesEnabled = true,
    videoError = videoError
)

@androidx.compose.runtime.Composable
private fun VideoScreenForTest(state: VideoState = mockVideoState()) {
    FunnyTheme {
        VideoScreen(
            state = state,
            controller = VideoPlayerController(),
            onToggleSubtitles = {},
            onVideoStarted = {},
            onVideoError = {},
            onRetryVideo = {},
            onGoToQuestions = {},
            onBack = {}
        )
    }
}
