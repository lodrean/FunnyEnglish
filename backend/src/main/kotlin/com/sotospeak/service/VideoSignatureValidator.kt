package com.sotospeak.service

/**
 * Проверка magic-bytes видео admin-загрузок (bd FunnyEnglish-7qf).
 * Расширение + content-type подделываются тривиально; подпись первых байтов
 * отсекает файлы, не являющиеся заявленным форматом (whitelist: mp4/webm).
 */
@Suppress("MagicNumber") // байтовые подписи форматов — сами являются данными
object VideoSignatureValidator {

    /** Первые байты, достаточные для всех проверяемых подписей. */
    private const val HEADER_SIZE = 12

    fun isAllowedVideo(firstBytes: ByteArray): Boolean {
        if (firstBytes.size < 4) return false
        return isMp4(firstBytes) || isWebm(firstBytes)
    }

    /** MP4/MOV-контейнер: смещение 4..7 = "ftyp". */
    private fun isMp4(b: ByteArray): Boolean =
        b.size >= 8 &&
            b[4] == 'f'.code.toByte() && b[5] == 't'.code.toByte() &&
            b[6] == 'y'.code.toByte() && b[7] == 'p'.code.toByte()

    /** WebM/MKV (EBML): 1A 45 DF A3. */
    private fun isWebm(b: ByteArray): Boolean =
        (b[0].toInt() and 0xFF) == 0x1A && (b[1].toInt() and 0xFF) == 0x45 &&
            (b[2].toInt() and 0xFF) == 0xDF && (b[3].toInt() and 0xFF) == 0xA3

    /** Читает первые [HEADER_SIZE] байт потока (поток остаётся читаемым далее). */
    fun readHeader(inputStream: java.io.InputStream): ByteArray =
        inputStream.use { it.readNBytes(HEADER_SIZE) }
}
