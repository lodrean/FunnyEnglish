package com.sotospeak.app.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.HTMLVideoElement

/**
 * WASM-реализация видеоплеера на HTML5 `<video>`.
 *
 * CMP wasmJs рендерит UI в canvas (canvas-only), DOM-интеропа нет, поэтому
 * video-элемент добавляется в document и позиционируется поверх canvas по
 * координатам Compose-области плеера ([updateViewport] из onGloballyPositioned;
 * canvas fullscreen → 1 layout px = 1 CSS px).
 *
 * Следствие: Compose-оверлей контролы ПОВЕРХ видео невозможны (DOM-элемент всегда
 * выше canvas) — на wasm контролы рисуются ПОД плеером ([supportsOverlayControls]=false),
 * а DOM-video скрывается в состояниях, где показываются Compose-оверлеи
 * (big-play до старта, «Начать заново» после конца) — см. [setDomVisible].
 */
actual class VideoPlayerController {

    private val _state = MutableStateFlow(VideoPlayerState())
    actual val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    actual val supportsOverlayControls: Boolean = false

    private var video: HTMLVideoElement? = null
    private var viewport: FloatArray? = null // x, y, width, height в CSS px

    actual fun prepare(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            _state.value = VideoPlayerState(error = "Invalid URL")
            return
        }
        val v = video ?: createVideoElement()
        _state.value = VideoPlayerState(isBuffering = true)
        if (v.src != sanitizedUrl) v.src = sanitizedUrl
        applyViewport(v)
    }

    actual fun play() {
        video?.play()
    }

    actual fun pause() {
        video?.pause()
    }

    actual fun seekTo(positionMs: Long) {
        val ms = positionMs.coerceAtLeast(0)
        video?.currentTime = ms / 1000.0
        _state.value = _state.value.copy(positionMs = ms, isEnded = false)
    }

    actual fun release() {
        video?.pause()
        video?.remove()
        video = null
        _state.value = VideoPlayerState()
    }

    /** Координаты области плеера в CSS-пикселях — вызывается из NativeVideoSurface. */
    fun updateViewport(x: Float, y: Float, width: Float, height: Float) {
        viewport = floatArrayOf(x, y, width, height)
        video?.let { applyViewport(it) }
    }

    /**
     * DOM-video виден только во время просмотра (playing или пауза после старта).
     * До старта и после конца он скрыт — там Compose-оверлеи (big-play / replay).
     */
    fun setDomVisible(visible: Boolean) {
        video?.style?.display = if (visible) "block" else "none"
    }

    private fun createVideoElement(): HTMLVideoElement {
        val v = document.createElement("video") as HTMLVideoElement
        v.style.position = "absolute"
        v.style.zIndex = "10"
        v.style.backgroundColor = "black"
        v.style.display = "none"
        v.style.cursor = "pointer"
        v.setAttribute("playsinline", "")
        v.setAttribute("preload", "metadata")

        // Клик по видео — play/pause
        v.addEventListener("click", { if (v.paused) v.play() else v.pause() })
        v.addEventListener("loadedmetadata", {
            _state.value = _state.value.copy(
                isReady = true,
                isBuffering = false,
                durationMs = (v.duration * 1000).toLong().coerceAtLeast(0)
            )
        })
        v.addEventListener("timeupdate", {
            _state.value = _state.value.copy(
                positionMs = (v.currentTime * 1000).toLong().coerceAtLeast(0)
            )
        })
        v.addEventListener("play", {
            _state.value = _state.value.copy(isPlaying = true, isEnded = false)
        })
        v.addEventListener("pause", {
            _state.value = _state.value.copy(isPlaying = false)
        })
        v.addEventListener("waiting", {
            _state.value = _state.value.copy(isBuffering = true)
        })
        v.addEventListener("playing", {
            _state.value = _state.value.copy(isBuffering = false, isPlaying = true)
        })
        v.addEventListener("ended", {
            _state.value = _state.value.copy(
                isPlaying = false,
                isEnded = true,
                positionMs = _state.value.durationMs
            )
        })
        v.addEventListener("error", {
            _state.value = _state.value.copy(
                error = "Video load error",
                isPlaying = false,
                isBuffering = false
            )
        })

        document.body?.appendChild(v)
        video = v
        return v
    }

    private fun applyViewport(v: HTMLVideoElement) {
        val vp = viewport ?: return
        v.style.left = "${vp[0]}px"
        v.style.top = "${vp[1]}px"
        v.style.width = "${vp[2]}px"
        v.style.height = "${vp[3]}px"
    }
}

/**
 * Чёрная подложка-якорь: по её координатам позиционируется DOM `<video>` поверх canvas.
 * Само видео — DOM-элемент (см. VideoPlayerController), здесь только синхронизация
 * геометрии и видимости.
 */
@Composable
actual fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier
) {
    val state by controller.state.collectAsState()
    // positionInRoot — в физических px (density = devicePixelRatio); DOM — в CSS px
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .background(Color.Black)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                controller.updateViewport(
                    x = pos.x / density,
                    y = pos.y / density,
                    width = coords.size.width / density,
                    height = coords.size.height / density
                )
            }
    )

    LaunchedEffect(state.isPlaying, state.isEnded, state.positionMs > 0L) {
        controller.setDomVisible(
            visible = !state.isEnded && (state.isPlaying || state.positionMs > 0L)
        )
    }
}
