# 02-execute — FunnyEnglish-qbq.2: INF чистка корня репозитория

## Что сделано

1. **Удалены из git и с диска 42 отладочных артефакта в корне** (git rm -f):
   - 41 PNG-скриншот: `admin-*.png` (8), `android-screen*.png` (2), `app-launch-attempt2.png`, `desktop-screenshot*.png` (2), `ds2-rebrand-dashboard.png`, `error-handling-*.png` (2), `gate-4.4-client-logs-ui.png`, `guest-analytics.png`, `image-word-match-success.png`, `sb-*.png` (7), `test-editor-hotspots.png`, `wasm*.png` (14);
   - 1 лог: `backend-run.err`.
2. **Удалены с диска незакоммиченные артефакты** (уже были в `.gitignore`, из git удалять нечего):
   - `backend-test.log` (32 МБ) — игнорировался глобальным `*.log`;
   - `maestro-cli.zip` (212 МБ) — игнорировался глобальным `*.zip`;
   - `admin-web-backup/` — уже была строка в `.gitignore` (от 2026-07-31).
3. **Дополнен `.gitignore`** секцией «Debug artifacts in repo root»: `/*.png`, `/*.err` (якорь на корень, чтобы не задеть `design-assets/`, snapshot'ы admin-web и т.п.).

## Проверки перед удалением

- `git ls-files` подтвердил: `backend-test.log`, `maestro-cli.zip`, `admin-web-backup/` в git НЕ закоммичены (расходится с формулировкой аудита AR-9 — фактически коммичены только PNG/err в корне).
- Поиск ссылок на каждый из 42 файлов по `*.md/*.html/*.ts/*.kt`: единственное упоминание `test-editor-hotspots.png` — текстовая ссылка в `docs/research/PROJECT-REVIEW-2026-08-28.md` (сам аудит перечисляет его как артефакт), не image-link. Битых ссылок нет.

## Изменённые файлы

- `.gitignore` — добавлены 2 паттерна + комментарий.
- 42 файла удалены (staged deletions, `git status`: 42× `D`).
- Коммитов/пушей НЕ делал (по инструкции драйвера).

## Как проверить

```bash
git status --short            # 42 staged удаления + M .gitignore
git ls-files | grep -v '/' | grep -E '\.(png|err|log|zip)$'   # пусто
git check-ignore -v some-root.png   # .gitignore:/*.png
```
