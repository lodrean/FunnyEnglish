package com.sotospeak.app.subtitles

/**
 * Слово субтитров с собственным таймингом (пословная подсветка транскрипта).
 * Источник тайминга — karaoke-таймкоды `<mm:ss.mmm>` в VTT, либо интерполяция
 * внутри cue пропорционально длине слова (см. [WebVttParser]).
 */
data class SubtitleWord(
    val text: String,
    val startMs: Long,
    val endMs: Long
)

/**
 * Cue субтитров (спека Part 2 §3.3).
 */
data class SubtitleCue(
    val startMs: Long,
    val endMs: Long,
    val text: String,          // многострочный текст cue; теги <b>/<i>/<c> вычищаем
    val words: List<SubtitleWord> = emptyList()  // пословные тайминги для транскрипта
)

/**
 * Минимальный парсер WebVTT (спека Part 2 §3.3, решение R4 — свой парсер, ~100 строк):
 * - пропускает шапку "WEBVTT" и NOTE-блоки;
 * - тайминги "00:00.500 --> 00:02.000" или "00:00:00.500 --> ...";
 * - игнорирует настройки cue (align/position — всё после второго пробела в строке тайминга);
 * - склеивает многострочный текст cue до пустой строки;
 * - мусорные строки вне cue игнорируются.
 *
 * Пословные тайминги (транскрипт с подсветкой):
 * - если в тексте cue есть karaoke-таймкоды `<mm:ss.mmm>` — слово получает start от
 *   ближайшего таймкода слева, end — от следующего таймкода (или конца cue);
 * - иначе слова интерполируются внутри [startMs, endMs] пропорционально длине слова
 *   (вес = числу букв/цифр, минимум 1).
 */
object WebVttParser {

    private val timingRegex = Regex(
        """(\d{1,2}:\d{2}(?::\d{2})?\.\d{3})\s+-->\s+(\d{1,2}:\d{2}(?::\d{2})?\.\d{3})"""
    )
    private val tagRegex = Regex("""</?[a-zA-Z][^>]*>""")
    /** Karaoke-таймкод внутри текста cue: <00:00.500> / <00:00:00.500> */
    private val karaokeRegex = Regex("""<(\d{1,2}:\d{2}(?::\d{2})?\.\d{3})>""")

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

            // Текст cue до пустой строки (сырой — с karaoke-таймкодами, если есть)
            val rawLines = mutableListOf<String>()
            while (i < lines.size && lines[i].isNotBlank()) {
                rawLines.add(lines[i].trim())
                i++
            }

            val rawText = rawLines.joinToString("\n").trim()
            // karaoke-таймкоды <mm:ss.mmm> не матчатся tagRegex — вычищаем отдельно
            val text = tagRegex.replace(rawText.replace(karaokeRegex, ""), "").trim()
            if (text.isNotEmpty() && endMs > startMs) {
                cues.add(SubtitleCue(startMs, endMs, text, extractWords(rawText, startMs, endMs)))
            }
        }

        return cues.sortedBy { it.startMs }
    }

    /**
     * Пословные тайминги: karaoke-таймкоды при наличии, иначе интерполяция по длине слова.
     * Сырые строки cue содержат karaoke-теги `<mm:ss.mmm>` (tagRegex их НЕ отделяет от слов).
     */
    internal fun extractWords(rawText: String, cueStartMs: Long, cueEndMs: Long): List<SubtitleWord> {
        // Токены: karaoke-таймкод | слово (с внутренними дефисами/апострофами)
        val tokenRegex = Regex("""<\d{1,2}:\d{2}(?::\d{2})?\.\d{3}>|[^\s<>]+""")
        val tokens = tokenRegex.findAll(rawText.replace("\n", " ")).map { it.value }.toList()

        val hasKaraoke = tokens.any { it.startsWith("<") }
        return if (hasKaraoke) {
            extractKaraokeWords(tokens, cueStartMs, cueEndMs)
        } else {
            interpolateWords(tokens, cueStartMs, cueEndMs)
        }
    }

    /** Karaoke: слово стартует от ближайшего таймкода слева, заканчивается следующим таймкодом. */
    private fun extractKaraokeWords(
        tokens: List<String>,
        cueStartMs: Long,
        cueEndMs: Long
    ): List<SubtitleWord> {
        data class Pending(val text: String, val startMs: Long)

        val words = mutableListOf<Pending>()
        var currentMs = cueStartMs
        for (token in tokens) {
            if (token.startsWith("<")) {
                karaokeRegex.find(token)?.let { currentMs = parseTimestamp(it.groupValues[1]) }
            } else {
                val clean = tagRegex.replace(token, "")
                if (clean.isNotEmpty()) words.add(Pending(clean, currentMs))
            }
        }
        return words.mapIndexed { idx, w ->
            val end = words.getOrNull(idx + 1)?.startMs ?: cueEndMs
            SubtitleWord(w.text, w.startMs, maxOf(end, w.startMs))
        }
    }

    /** Интерполяция: окно [cueStartMs, cueEndMs] делится между словами пропорционально их длине. */
    private fun interpolateWords(
        tokens: List<String>,
        cueStartMs: Long,
        cueEndMs: Long
    ): List<SubtitleWord> {
        val words = tokens.map { tagRegex.replace(it, "") }.filter { it.isNotEmpty() }
        if (words.isEmpty()) return emptyList()

        val weights = words.map { w -> w.count { it.isLetterOrDigit() }.coerceAtLeast(1) }
        val totalWeight = weights.sum()
        val duration = cueEndMs - cueStartMs

        val result = mutableListOf<SubtitleWord>()
        var cursor = cueStartMs
        words.forEachIndexed { idx, word ->
            val share = duration * weights[idx] / totalWeight
            val end = if (idx == words.lastIndex) cueEndMs else cursor + share
            result.add(SubtitleWord(word, cursor, end))
            cursor = end
        }
        return result
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
