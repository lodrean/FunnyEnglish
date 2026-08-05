# Тестирование на устройстве в домашней Wi-Fi сети (LAN)

> Как запустить backend на домашнем компе и подключить к нему: (а) мобильное
> приложение на телефоне по Wi-Fi (QA-сборка с debug-меню), (б) другое устройство
> (ноутбук) в той же сети. Обновлено: 2026-08-02.

Все шаги предполагают, что **телефон/ноутбук и комп с backend в одной Wi-Fi сети**.

---

## 0. TL;DR

```bash
ipconfig                                  # узнать LAN-IP компа, напр. 192.168.1.50
# docker/.env: S3_PUBLIC_URL=http://192.168.1.50:9000/sotospeak
docker compose up -d                      # поднять backend+postgres+minio+admin
./gradlew :app:assembleQa                 # собрать QA-APK
adb install -r app/build/outputs/apk/qa/app-qa.apk
# на телефоне: Профиль → 7 тапов по версии → Debug Menu → http://192.168.1.50:8080/
# → «Проверить соединение» → «Сохранить» → перезапустить приложение
```

---

## 1. Подготовка домашнего компа

### 1.1. Узнать LAN-IP

```bash
ipconfig    # Windows: «IPv4-адрес» активного адаптера, напр. 192.168.1.50
```

Дальше везде `<LAN-IP>` = этот адрес. **При смене Wi-Fi сети IP меняется — повторить настройку.**

### 1.2. Запустить backend-стек

```bash
docker compose up -d        # postgres:5432, minio:9000/9001, backend:8080, admin:3000
```

Все порты в dev-compose проброшены на `0.0.0.0` — снаружи доступны сразу, препятствие
только одно: **брандмауэр Windows**.

### 1.3. Открыть порты в брандмауэре Windows

PowerShell **от администратора**:

```powershell
New-NetFirewallRule -DisplayName "SoToSpeak backend 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
New-NetFirewallRule -DisplayName "SoToSpeak minio 9000"   -Direction Inbound -Protocol TCP -LocalPort 9000 -Action Allow
New-NetFirewallRule -DisplayName "SoToSpeak admin 3000"   -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow
```

(Или GUI: «Брандмауэр Защитника Windows» → «Правила для входящих» → «Создать правило» → Порт → TCP → 8080, 9000, 3000.)

Удалить после тестирования: `Remove-NetFirewallRule -DisplayName "SoToSpeak*"`.

### 1.4. Медиа (видео + субтитры из админки) — ОБЯЗАТЕЛЬНО

Backend отдаёт URL медиа через `S3_PUBLIC_URL` (BUG-004). Дефолт в dev-compose —
`http://localhost:9000/sotospeak`: на телефоне `localhost` — это сам телефон,
видео не откроется. Выставить LAN-IP:

```bash
# docker/.env (или переменная окружения)
S3_PUBLIC_URL=http://<LAN-IP>:9000/sotospeak
```

```bash
docker compose up -d backend    # пересоздать backend с новым env
```

Проверка с телефона: открыть в браузере URL любого видео из админки —
`http://<LAN-IP>:9000/sotospeak/...` должен отдавать файл.

> Старые записи в БД с `localhost`/внутренними URL нормализуются на лету (фикс BUG-004),
> но новые загрузки возьмут `S3_PUBLIC_URL` сразу.

### 1.5. Проверка доступности с другого устройства

С телефона/ноутбука в той же сети:

```
http://<LAN-IP>:8080/api/actuator/health   →  {"status":"UP"}
```

(префикс `/api` обязателен — context-path backend). Если не открывается — см. §5.

---

## 2. QA-сборка приложения с debug-меню

### 2.1. Собрать и установить

```bash
./gradlew :app:assembleQa
adb install -r app/build/outputs/apk/qa/app-qa.apk
```

QA-сборка — отдельное приложение (`com.sotospeak.app.qa`), ставится **рядом** с
обычной, ничего не затирает. В ней: cleartext http разрешён, включено debug-меню.

Альтернатива без adb: скопировать `app-qa.apk` на телефон и установить вручную
(разрешить «установку из неизвестных источников»).

### 2.2. Указать адрес backend в debug-меню

1. Открыть приложение → вкладка **«Профиль»**.
2. Внизу экрана — строка версии («So to speak v1.0.0-qa»). **Тапнуть по ней 7 раз**
   (серия не дольше 2 с между тапами) → откроется **Debug Menu**.
3. В поле «Backend URL» ввести `http://<LAN-IP>:8080/`
4. **«Проверить соединение»** — должно быть «✅ Backend доступен».
5. **«Сохранить»** и **перезапустить приложение** (URL применяется при старте).
6. «Сбросить» — вернуться к URL, зашитому в сборку.

В Debug Menu также: версия/платформа, effective URL и его источник, кнопка
**«Отправить логи»** (принудительный flush очереди WARN/ERROR на backend —
присланные логи смотреть в админке, раздел **Logs**).

### 2.3. Альтернатива без меню (URL на этапе сборки)

```bash
./gradlew :app:assembleQa -PSOTOSPEAK_API_BASE_URL=http://<LAN-IP>:8080/
```

Минус: при смене сети/IP — пересборка.

---

## 3. Подключение с другого ноутбука (та же Wi-Fi сеть)

| Что | URL / команда |
|---|---|
| Админка (docker) | `http://<LAN-IP>:3000` |
| API напрямую | `http://<LAN-IP>:8080/api/...` (health: `.../api/actuator/health`) |
| MinIO console | `http://<LAN-IP>:9001` (minioadmin/minioadmin) |
| Desktop-приложение | `SOTOSPEAK_API_BASE_URL=http://<LAN-IP>:8080 ./gradlew :composeApp:run` |
| Admin-web dev (vite) | `VITE_API_URL=http://<LAN-IP>:8080/api npm run dev -- --host` |

Для vite-варианта админки CORS: dev-compose разрешает только `localhost`-origins.
Запросы с `http://<LAN-IP>:5173` могут резаться CORS — тогда добавить origin в
`CORS_ORIGINS` backend'а (`docker compose up -d backend` после правки) или
пользоваться docker-админкой (3000, запросы проксируются nginx — CORS не участвует).

---

## 4. Полный сценарий приёмки на телефоне

1. Стек поднят, firewall открыт, `S3_PUBLIC_URL` выставлен (§1).
2. QA-APK установлен, в Debug Menu введён `http://<LAN-IP>:8080/`, соединение проверено (§2).
3. В админке (`http://<LAN-IP>:3000`) загрузить видео + субтитры в топик, опубликовать.
4. На телефоне: Библиотека → тема → топик → видео играет, субтитры переключаются.
5. Training/Practice — запись ответов; Practice требует регистрации (гейт).
6. Ошибки приложения уходят на backend: админка → **Logs** (фильтр platform=android).

---

## 5. Troubleshooting

| Симптом | Причина / лечение |
|---|---|
| `http://<LAN-IP>:8080/api/actuator/health` не открывается с телефона | Firewall (§1.3); телефон в «гостевой» Wi-Fi сети — на роутере отключить **AP isolation / изоляцию клиентов** |
| Backend доступен, но видео не играет | `S3_PUBLIC_URL` не выставлен/старый backend-контейнер — §1.4 + `docker compose up -d backend` |
| «❌ Не удалось подключиться» в Debug Menu | Опечатка в URL (нужен `http://` и порт 8080); backend не запущен; IP сменился (`ipconfig`) |
| Приложение вообще не ходит в сеть на эмуляторе | Proxyman/Charles на хосте: системный прокси `10.0.2.2:9090` и/или Proxyman VPN на эмуляторе (memory.md №13/№28) — выключить прокси/VPN на время теста |
| `adb install` — INSTALL_FAILED_UPDATE_INCOMPATIBLE | Подпись QA отличается от установленной: `adb uninstall com.sotospeak.app.qa` и повторить |
| CORS-ошибки в vite-админке с другого ноутбука | §3: добавить origin в `CORS_ORIGINS` или использовать docker-админку на 3000 |
| Логи не видны в админке | Логи уходят только уровня WARN/ERROR; проверить Debug Menu → «Отправить логи» и что устройство ходит на тот же backend |

**Важно:** cleartext `http://` разрешён только в debug/qa-сборках. Release-сборка
по `http://<LAN-IP>` работать не будет (`usesCleartextTraffic=false`) — это осознанно.
