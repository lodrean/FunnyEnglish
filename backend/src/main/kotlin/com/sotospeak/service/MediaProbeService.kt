package com.sotospeak.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/** Результат ffprobe-дампа файла (bd FunnyEnglish-h3l.7). */
data class MediaProbeResult(
    val videoCodec: String?,
    val audioCodec: String?,
    val containerFormats: List<String>
)

/**
 * Валидация загружаемого видео через ffprobe (bd FunnyEnglish-h3l.7):
 * ученический плеер (ExoPlayer/HTML5) проигрывает узкий набор кодеков —
 * файл с неподдерживаемым кодеком отсекается на загрузке, а не у ученика.
 * Транскодирование — отдельная задача (bd-остаток), здесь только fail-fast.
 *
 * Если ffprobe недоступен в окружении (локальный запуск без ffmpeg) —
 * probe возвращает null и валидация пропускается (fail-open), чтобы не
 * ломать dev-контуры; Docker-образ содержит ffmpeg всегда.
 */
@Service
class MediaProbeService(
    @Value("\${app.media.ffprobe-path:ffprobe}") private val ffprobePath: String,
    @Value("\${app.media.probe-timeout-seconds:15}") private val probeTimeoutSeconds: Long
) {
    private val logger = LoggerFactory.getLogger(MediaProbeService::class.java)
    private val objectMapper = ObjectMapper()

    /** null — ffprobe недоступен (валидация пропускается). */
    fun probe(file: Path): MediaProbeResult? = try {
        runProbe(file)
    } catch (e: IOException) {
        logger.warn("ffprobe недоступен ({}), валидация видео пропущена: {}", ffprobePath, e.message)
        null
    }

    private fun runProbe(file: Path): MediaProbeResult? {
        val process = ProcessBuilder(
            ffprobePath, "-v", "quiet", "-print_format", "json",
            "-show_format", "-show_streams", file.toAbsolutePath().toString()
        ).start()
        val output = try {
            process.inputStream.readBytes().toString(Charsets.UTF_8)
        } finally {
            process.waitFor(probeTimeoutSeconds, TimeUnit.SECONDS)
            process.destroyForcibly()
        }
        if (process.exitValue() != 0) {
            logger.warn("ffprobe exit={} для {}: файл не распознан как медиа", process.exitValue(), file.fileName)
            return MediaProbeResult(videoCodec = null, audioCodec = null, containerFormats = emptyList())
        }
        return parse(output)
    }

    /** Чистый парсер дампа ffprobe — покрыт unit-тестами. */
    fun parse(json: String): MediaProbeResult? =
        runCatching { doParse(json) }
            .onFailure { logger.warn("Не удалось разобрать вывод ffprobe: {}", it.message) }
            .getOrNull()

    @Throws(IOException::class)
    private fun doParse(json: String): MediaProbeResult? {
        val root: JsonNode = objectMapper.readTree(json)
        val streams = root.get("streams") ?: return null
        var videoCodec: String? = null
        var audioCodec: String? = null
        streams.forEach { stream ->
            when (stream.path("codec_type").asText()) {
                "video" -> if (videoCodec == null) videoCodec = stream.path("codec_name").asText(null)
                "audio" -> if (audioCodec == null) audioCodec = stream.path("codec_name").asText(null)
            }
        }
        val formats = root.path("format").path("format_name").asText("")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return MediaProbeResult(videoCodec = videoCodec, audioCodec = audioCodec, containerFormats = formats)
    }

    /**
     * Политика кодеков по расширению контейнера (whitelist, bd h3l.7):
     * mp4/m4v/mov → h264/hevc/av1; webm → vp8/vp9/av1.
     * Возвращает null, если файл допустим; иначе — человеческую причину отказа.
     */
    fun rejectReason(extension: String, result: MediaProbeResult): String? {
        val allowed = when (extension) {
            "webm" -> WEBM_CODECS
            else -> MP4_CODECS
        }
        val codec = result.videoCodec?.lowercase()
        return when {
            codec == null -> "В файле нет видеопотока"
            codec in allowed -> null
            else ->
                "Видеокодек '$codec' не поддерживается плеером — " +
                    "перекодируйте в ${allowed.firstOrNull() ?: "H.264"}"
        }
    }

    companion object {
        private val MP4_CODECS = setOf("h264", "hevc", "av1")
        private val WEBM_CODECS = setOf("vp8", "vp9", "av1")
    }
}
