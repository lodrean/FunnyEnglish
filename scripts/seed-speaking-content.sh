#!/usr/bin/env bash
# Seed speaking-контента для тестового окружения (staging/dev):
# библиотека «Разговорный английский» → топик «Знакомство» (видео + WebVTT + 3 вопроса, опубликовано).
# Идемпотентно: если библиотека с таким названием уже есть — выходим.
#
# Использование:
#   ./scripts/seed-speaking-content.sh [BASE_URL] [ADMIN_EMAIL] [ADMIN_PASSWORD]
#   BASE_URL — с /api (default: http://localhost:8180/api — staging)
set -euo pipefail

BASE_URL="${1:-http://localhost:8180/api}"
ADMIN_EMAIL="${2:-admin@sotospeak.com}"
ADMIN_PASSWORD="${3:-admin123}"
FIXTURES_DIR="$(dirname "$0")/../admin-web/e2e/fixtures"

echo "== Seed speaking-контента → $BASE_URL"

TOKEN=$(curl -sf -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
AUTH="Authorization: Bearer $TOKEN"
echo "✓ Логин $ADMIN_EMAIL"

# Идемпотентность: библиотека уже сидирована?
EXISTING=$(curl -sf "$BASE_URL/admin/speaking/libraries" -H "$AUTH")
if echo "$EXISTING" | grep -q "Разговорный английский"; then
  echo "✓ Библиотека «Разговорный английский» уже существует — пропуск"
  exit 0
fi

# Upload медиа (видео + субтитры)
VIDEO_URL=$(curl -sf -X POST "$BASE_URL/admin/media/upload" -H "$AUTH" \
  -F "file=@$FIXTURES_DIR/sample-video.mp4" -F "folder=speaking" | python -c "import sys,json;print(json.load(sys.stdin)['url'])")
SUBTITLES_URL=$(curl -sf -X POST "$BASE_URL/admin/media/upload" -H "$AUTH" \
  -F "file=@$FIXTURES_DIR/sample-subtitles.vtt" -F "folder=speaking" | python -c "import sys,json;print(json.load(sys.stdin)['url'])")
echo "✓ Upload: $VIDEO_URL / $SUBTITLES_URL"

# Library
LIB_ID=$(curl -sf -X POST "$BASE_URL/admin/speaking/libraries" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"title":"Разговорный английский","description":"Живые диалоги для everyday English","displayOrder":0,"isPublished":true}' \
  | python -c "import sys,json;print(json.load(sys.stdin)['id'])")
echo "✓ Library: $LIB_ID"

# Topic
TOPIC_ID=$(curl -sf -X POST "$BASE_URL/admin/speaking/topics" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"libraryId\":\"$LIB_ID\",\"title\":\"Знакомство\",\"description\":\"Представляемся и отвечаем на простые вопросы\",\"displayOrder\":0,\"isPublished\":true}" \
  | python -c "import sys,json;print(json.load(sys.stdin)['id'])")
echo "✓ Topic: $TOPIC_ID"

# Video (upsert)
curl -sf -X PUT "$BASE_URL/admin/speaking/topics/$TOPIC_ID/video" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"videoUrl\":\"$VIDEO_URL\",\"subtitleUrl\":\"$SUBTITLES_URL\",\"durationSeconds\":60}" > /dev/null
echo "✓ Video + субтитры привязаны"

# Questions
for i in 0 1 2; do
  case $i in
    0) Q="What is your name?";;
    1) Q="Where are you from?";;
    2) Q="What do you do?";;
  esac
  curl -sf -X POST "$BASE_URL/admin/speaking/topics/$TOPIC_ID/questions" -H "$AUTH" -H "Content-Type: application/json" \
    -d "{\"text\":\"$Q\",\"displayOrder\":$i}" > /dev/null
done
echo "✓ 3 вопроса добавлены"

# Проверка: публичный список
curl -sf "$BASE_URL/public/speaking/libraries" | grep -q "Разговорный английский" \
  && echo "✅ Seed завершён: библиотека видна публично"
