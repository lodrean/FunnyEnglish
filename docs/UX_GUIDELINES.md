# FunnyEnglish UX Guidelines

## Общие принципы

### 1. Навигация

#### Кнопка "Назад"
- **Всегда** должен быть способ вернуться назад
- На внутренних экранах использовать `TopAppBar` с `navigationIcon`
- На диалогах использовать явную кнопку "Отмена" или иконку закрытия
- Android: поддержка системной кнопки назад

```kotlin
// Правильно
TopAppBar(
    title = { Text("Заголовок") },
    navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
        }
    }
)

// Для диалогов
AlertDialog(
    onDismissRequest = onDismiss,
    title = { 
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Заголовок")
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Закрыть")
            }
        }
    },
    ...
)
```

### 2. Состояния экрана

#### Загрузка
- Показывать `CircularProgressIndicator` при первой загрузке
- Использовать skeleton screens для списков (опционально)
- Блокировать UI при отправке форм

```kotlin
if (state.isLoading && state.data == null) {
    LoadingIndicator() // На весь экран
    return
}
```

#### Пустое состояние
- Показывать иллюстрацию и поясняющий текст
- Предлагать действие (кнопку)
- Не оставлять пустой экран

```kotlin
if (items.isEmpty()) {
    EmptyStateView(
        icon = Icons.Default.Inbox,
        title = "Пока ничего нет",
        description = "Начните с создания первого элемента",
        actionButton = { Button(onClick = onCreate) { Text("Создать") } }
    )
}
```

#### Ошибка
- Показывать понятное сообщение об ошибке
- Предлагать повторить действие
- Не показывать технические детали

```kotlin
if (state.error != null) {
    ErrorMessage(
        message = state.error,
        onRetry = onRetry
    )
}
```

### 3. Формы

#### Поля ввода
- Использовать `OutlinedTextField` вместо обычного
- Показывать иконку поля слева
- Добавлять `helperText` для подсказок
- Валидация в реальном времени

```kotlin
OutlinedTextField(
    value = value,
    onValueChange = onChange,
    label = { Text("Email") },
    leadingIcon = { Icon(Icons.Default.Email, null) },
    supportingText = { Text("Введите ваш email") },
    isError = hasError,
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next
    )
)
```

#### Кнопки
- Primary: заполненная, основное действие
- Secondary: outlined, альтернативное действие
- Text: минимальная, дополнительное действие
- Располагать кнопки внизу экрана
- Primary кнопка должна быть самой правой/нижней

### 4. Отзывчивость

#### Обратная связь
- Snackbar для успешных действий
- Toast для кратких уведомлений
- Dialog для подтверждения важных действий

```kotlin
// Успешное действие
scope.launch {
    snackbarHostState.showSnackbar(
        message = "Сохранено!",
        actionLabel = "Отмена",
        duration = SnackbarDuration.Short
    )
}
```

#### Прогресс
- Линейный прогресс для длительных операций
- Круговой прогресс для загрузки
- Не блокировать UI без необходимости

### 5. Визуальная иерархия

#### Типографика
- Заголовки: `headlineSmall` - `headlineMedium`
- Подзаголовки: `titleLarge` - `titleMedium`
- Текст: `bodyLarge` - `bodyMedium`
- Подписи: `labelMedium` - `labelSmall`

#### Цвета
- Primary: основные действия, активные элементы
- Secondary: второстепенные действия
- Surface: карточки, фоны
- Error: ошибки, предупреждения
- On- colors: текст на соответствующих фонах

### 6. Адаптивность

#### Отступы
- xs: 4.dp
- sm: 8.dp
- md: 16.dp
- lg: 24.dp
- xl: 32.dp

#### Размеры касания
- Минимальный размер: 48.dp
- Расстояние между элементами: 8.dp

### 7. Accessibility

- Все иконки должны иметь `contentDescription`
- Контраст текста не менее 4.5:1
- Поддержка TalkBack/VoiceOver
- Увеличенные шрифты должны работать корректно

## Проверка экранов

### ✅ LoginScreen
- [x] Валидация полей
- [x] Скрытие/показ пароля
- [x] Состояние загрузки
- [x] Обработка ошибок

### ✅ RegisterScreen  
- [x] Все поля ввода
- [x] Индикатор загрузки
- [x] Кнопка назад (встроена в навигацию)

### ✅ HomeScreen
- [x] Pull-to-refresh (через LaunchedEffect)
- [x] Обработка пустых состояний
- [x] Навигация на профиль

### ⚠️ GroupsScreen
- [ ] **Нужно добавить**: кнопку назад в JoinGroupDialog
- [x] Пустое состояние
- [x] Загрузка
- [x] Обработка ошибок

### ✅ GroupDetailScreen
- [x] Кнопка назад в TopAppBar
- [x] Меню действий
- [x] Загрузка

### ✅ ProfileScreen
- [x] Аватар с инициалами
- [x] Статистика
- [x] Меню настроек

### ✅ TestPlayScreen
- [x] Таймер
- [x] Прогресс
- [x] Кнопка закрытия (X)
- [x] Результаты

### ✅ AchievementsScreen
- [x] Фильтры
- [x] Прогресс
- [x] Пустые состояния

## Исправления

### JoinGroupDialog - ДОЛЖЕН быть исправлен

```kotlin
@Composable
private fun JoinGroupDialog(
    inviteCode: String,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Присоединиться к группе")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column {
                Text(
                    "Введите код приглашения от вашего преподавателя",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(SpaceMd))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = onCodeChange,
                    label = { Text("Код приглашения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = { Text("Код состоит из 6-8 символов") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onJoin,
                enabled = inviteCode.length >= 4 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Присоединиться")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
```
