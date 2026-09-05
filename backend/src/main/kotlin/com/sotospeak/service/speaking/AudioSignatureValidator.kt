package com.sotospeak.service.speaking

/**
 * Проверка magic-bytes аудио practice-записей (bd FunnyEnglish-nj2.8).
 * Расширение + content-type подделываются тривиально; подпись первых байтов
 * отсекает файлы, не являющиеся заявленным форматом (whitelist спеки:
 * m4a/aac/mp3/wav/ogg — SPEAKING_TRAINER_SPEC_PART2 §2.6).
 */
object AudioSignatureValidator {

    /** Первые байты, достаточные для всех проверяемых подписей. */
    private const val HEADER_SIZE = 12

    fun isAllowedAudio(firstBytes: ByteArray): Boolean {
        if (firstBytes.size < 4) return false
        return isMp4M4a(firstBytes) || isMp3OrAac(firstBytes) || isWav(firstBytes) || isOgg(firstBytes)
    }

    /** MP4/M4A-контейнер: смещение 4..7 = "ftyp". */
    private fun isMp4M4a(b: ByteArray): Boolean =
        b.size >= 8 &&
            b[4] == 'f'.code.toByte() && b[5] == 't'.code.toByte() &&
            b[6] == 'y'.code.toByte() && b[7] == 'p'.code.toByte()

    /** MP3 (ID3-тег или frame sync 0xFFEx) и сырой AAC ADTS (тот же sync). */
    private fun isMp3OrAac(b: ByteArray): Boolean {
        if (b[0] == 'I'.code.toByte() && b[1] == 'D'.code.toByte() && b[2] == '3'.code.toByte()) return true
        return (b[0].toInt() and 0xFF) == 0xFF && (b[1].toInt() and 0xE0) == 0xE0
    }

    /** WAV: "RIFF" + "WAVE" на смещении 8. */
    private fun isWav(b: ByteArray): Boolean =
        b.size >= 12 &&
            b[0] == 'R'.code.toByte() && b[1] == 'I'.code.toByte() &&
            b[2] == 'F'.code.toByte() && b[3] == 'F'.code.toByte() &&
            b[8] == 'W'.code.toByte() && b[9] == 'A'.code.toByte() &&
            b[10] == 'V'.code.toByte() && b[11] == 'E'.code.toByte()

    /** OGG: "OggS". */
    private fun isOgg(b: ByteArray): Boolean =
        b[0] == 'O'.code.toByte() && b[1] == 'g'.code.toByte() &&
            b[2] == 'g'.code.toByte() && b[3] == 'S'.code.toByte()

    /** Читает первые [HEADER_SIZE] байт потока (поток остаётся читаемым далее). */
    fun readHeader(inputStream: java.io.InputStream): ByteArray =
        inputStream.use { it.readNBytes(HEADER_SIZE) }
}
