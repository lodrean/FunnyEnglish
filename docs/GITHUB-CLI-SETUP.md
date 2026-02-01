# Настройка GitHub CLI на Windows

## Установка

### Вариант 1: winget (рекомендуется)
```powershell
winget install GitHub.cli
```

### Вариант 2: Chocolatey
```powershell
choco install gh
```

### Вариант 3: Scoop
```powershell
scoop install gh
```

### Вариант 4: Скачать installer
https://github.com/cli/cli/releases/latest → `gh_X.X.X_windows_amd64.msi`

## Авторизация

После установки **перезапустите терминал** и выполните:

```bash
gh auth login
```

Ответьте на вопросы:
1. **What account do you want to log into?** → `GitHub.com`
2. **What is your preferred protocol?** → `HTTPS`
3. **Authenticate Git with your GitHub credentials?** → `Yes`
4. **How would you like to authenticate?** → `Login with a web browser`

Откроется браузер → войдите в GitHub → введите код из терминала.

## Проверка

```bash
gh auth status
```

Должно показать:
```
✓ Logged in to github.com as lodrean
✓ Git operations for github.com configured to use https protocol.
✓ Token: gho_****
```

## Создание Issues

После авторизации можно создавать issues:

```bash
# Один issue
gh issue create --title "Bug title" --body "Description" --label "bug"

# Из файла
gh issue create --title "Bug" --body-file docs/issues/BUG-001.md

# Посмотреть issues
gh issue list

# Посмотреть конкретный issue
gh issue view 123
```

## Полезные команды

```bash
# Статус PR
gh pr status

# Создать PR
gh pr create --title "Feature" --body "Description"

# Посмотреть CI статус
gh run list

# Клонировать репо
gh repo clone owner/repo
```

## Настройка для Claude Code

После установки gh CLI, MCP GitHub server должен подхватить авторизацию автоматически.
Перезапустите Claude Code после настройки gh auth.
