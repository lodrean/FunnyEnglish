package com.funnyenglish.app.subtitles

/**
 * Cue субтитров (спека Part 2 §3.3).
 */
data class SubtitleCue(
    val startMs: Long,
    val endMs: Long,
    val text: String          // многострочный текст cue; теги <b>/<i>/<c> вычищаем
)

/**
 * Минимальный парсер WebVTT (спека Part 2 §3.3, решение R4 — свой парсер, ~100 строк):
 * - пропускает шапку "WEBVTT" и NOTE-блоки;
 * - тайминги "00:00.500 --> 00:02.000" или "00:00:00.500 --> ...";
 * - игнорирует настройки cue (align/position — всё после второго пробела в строке тайминга);
 * - склеивает многострочный текст cue до пустой строки;
 * - мусорные строки вне cue игнорируются.
 */
object WebVttParser {

    private val timingRegex = Regex(
        """(\d{1,2}:\d{2}(?::\d{2})?\.\d{3})\s+-->\s+(\d{1,2}:\d{2}(?::\d{2})?\.\d{3})"""
    )
    private val tagRegex = Regex("""</?[a-zA-Z][^>]*>""")

    fun parse(vtt: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val lines = vtt.replace("\r\n", "\n").replace('\r', '\n').split("\n")

        var i = 0
        // Шапка: первая строка WEBVTT (+ возможный текст после)
        if (lines.firstOrNull()?.startsWith("WEBVTT") == true) i = 1

        while (i < lines.size) {
            val line = lines[i].trim()

            // NOTE-блок (многострочный): пропускаем до пустой строки
            if (line.startsWith("NOTE")) {
                while (i < lines.size && lines[i].isNotBlank()) i++
                i++
                continue
            }

            // Пустая строка — разделитель блоков
            if (line.isEmpty()) {
                i++
                continue
            }

            val match = timingRegex.find(line)
            if (match == null) {
                // Возможно, это идентификатор cue — тайминг на следующей строке
                val next = lines.getOrNull(i + 1)?.trim().orEmpty()
                if (timingRegex.containsMatchIn(next)) {
                    i++
                    continue
                }
                // Мусорная строка вне cue
                i++
                continue
            }

            val startMs = parseTimestamp(match.groupValues[1])
            val endMs = parseTimestamp(match.groupValues[2])
            i++

            // Текст cue до пустой строки
            val textLines = mutableListOf<String>()
            while (i < lines.size && lines[i].isNotBlank()) {
                textLines.add(lines[i].trim())
                i++
            }

            val text = textLines.joinToString("\n") { tagRegex.replace(it, "") }.trim()
            if (text.isNotEmpty() && endMs > startMs) {
                cues.add(SubtitleCue(startMs, endMs, text))
            }
        }

        return cues.sortedBy { it.startMs }
    }

    /** hh:mm:ss.mmm | mm:ss.mmm → миллисекунды */
    private fun parseTimestamp(ts: String): Long {
        val parts = ts.split(":")
        val (hours, minutes, secMs) = when (parts.size) {
            3 -> Triple(parts[0].toLong(), parts[1].toLong(), parts[2])
            else -> Triple(0L, parts[0].toLong(), parts[1])
        }
        val secParts = secMs.split(".")
        val seconds = secParts[0].toLong()
        val millis = secParts.getOrElse(1) { "0" }.padEnd(3, '0').take(3).toLong()
        return hours * 3_600_000 + minutes * 60_000 + seconds * 1_000 + millis
    }
}
