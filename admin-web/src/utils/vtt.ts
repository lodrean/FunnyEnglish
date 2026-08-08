/**
 * Извлечение полного текста транскрипта из WebVTT — для превью в админке.
 * Полный текст видео формируется из субтитров, отдельный файл транскрипта не нужен.
 * Логика зеркалит клиентский WebVttParser (composeApp): пропускаем шапку WEBVTT,
 * NOTE-блоки, строки таймингов и идентификаторы cue; теги <b>/<c>/<mm:ss.mmm> чистим.
 */

/** Строка тайминга cue: "00:00.500 --> 00:02.000" (+ опциональные настройки) */
const TIMING_RE = /\d{1,2}:\d{2}(?::\d{2})?\.\d{3}\s+-->/;
/** Любые угловые теги: <b>, </c.class>, karaoke <00:00.500> */
const TAG_RE = /<\/?[^>]+>/g;

export function extractVttTranscript(vtt: string): string {
  const lines = vtt.replace(/\r\n?/g, '\n').split('\n');
  const textLines: string[] = [];

  let i = lines[0]?.startsWith('WEBVTT') ? 1 : 0;
  let prevWasTiming = false;

  while (i < lines.length) {
    const line = lines[i].trim();

    // NOTE-блок — до пустой строки
    if (line.startsWith('NOTE')) {
      while (i < lines.length && lines[i].trim() !== '') i++;
      prevWasTiming = false;
      i++;
      continue;
    }

    if (line === '') {
      prevWasTiming = false;
      i++;
      continue;
    }

    if (TIMING_RE.test(line)) {
      prevWasTiming = true;
      i++;
      continue;
    }

    // Идентификатор cue (строка перед таймингом) не должен попадать в текст:
    // если следующая строка — тайминг, это идентификатор
    const next = lines[i + 1]?.trim() ?? '';
    if (!prevWasTiming && TIMING_RE.test(next)) {
      i++;
      continue;
    }

    const clean = line.replace(TAG_RE, '').trim();
    if (clean) textLines.push(clean);
    i++;
  }

  return textLines.join(' ').replace(/\s{2,}/g, ' ').trim();
}
