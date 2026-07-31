# Конвенции кода FunnyEnglish

- DI: Koin 4.0.0. `val xxxModule = module { single {...}; viewModel {...} }` (см. composeApp/.../app/di/AppModule.kt). Старт: KoinApplication { modules(appModule) } в App.kt (НЕ startKoin). VM через koinViewModel(). Платформенный конфиг — expect/actual provideAppConfig().
- Presentation: MVI на StateFlow. Тройка файлов: XxxState.kt (data class), XxxAction.kt (sealed), XxxEvent.kt. ViewModel: MutableStateFlow + Channel<Event>.receiveAsFlow() + onAction(). Пример: feature-home/.../presentation/HomeViewModel.kt. Ошибки — UiText?.
- Data: Ktor 3.0.2 + safeCall + Result. Repository: `override suspend fun getX(): Result<X, DataError.Network> = safeCall { api.getX() }` (feature-home/.../data/HomeRepositoryImpl.kt). Json: ignoreUnknownKeys=true, isLenient=true. Токен: PersistentTokenProvider (multiplatform-settings, ключ auth_token). Картинки: Coil 3.
- ГРАБЛЯ: Result/DataError импортировать из core/domain/util/ (в core/domain/ — устаревшие дубли).
- Навигация composeApp: sealed class AppScreen + remember mutableStateOf в App.kt — без navigation-compose.
- Admin-web: страницы src/pages/, axios-клиент src/api/client.ts (токен в localStorage), E2E — Playwright Page Objects в e2e/pages/.
