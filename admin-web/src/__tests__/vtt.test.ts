import { describe, expect, it } from 'vitest';
import { extractVttTranscript } from '../utils/vtt';

describe('extractVttTranscript', () => {
  it('извлекает текст cue без таймингов и шапки', () => {
    const vtt = [
      'WEBVTT',
      '',
      '00:00.500 --> 00:02.000',
      'Hello there',
      '',
      '00:02.000 --> 00:04.000',
      'How are you?',
      '',
    ].join('\n');
    expect(extractVttTranscript(vtt)).toBe('Hello there How are you?');
  });

  it('чистит теги и karaoke-таймкоды', () => {
    const vtt = [
      'WEBVTT',
      '',
      '00:00.000 --> 00:03.000',
      '<00:00.000><b>Hello</b> <00:01.000>brave <00:02.000>world',
      '',
    ].join('\n');
    expect(extractVttTranscript(vtt)).toBe('Hello brave world');
  });

  it('пропускает NOTE-блоки и идентификаторы cue', () => {
    const vtt = [
      'WEBVTT',
      '',
      'NOTE comment',
      'spanning lines',
      '',
      'cue-42',
      '00:01.000 --> 00:02.000 align:start',
      'Real text',
      '',
    ].join('\n');
    expect(extractVttTranscript(vtt)).toBe('Real text');
  });

  it('склеивает многострочный текст cue и обрабатывает CRLF', () => {
    const vtt = 'WEBVTT\r\n\r\n00:01.000 --> 00:03.000\r\nFirst line\r\nsecond line\r\n';
    expect(extractVttTranscript(vtt)).toBe('First line second line');
  });

  it('пустой/мусорный ввод → пустая строка', () => {
    expect(extractVttTranscript('')).toBe('');
    expect(extractVttTranscript('WEBVTT\n\nrandom garbage')).toBe('random garbage');
  });
});
