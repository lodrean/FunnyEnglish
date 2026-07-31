---
name: android-clean-architecture
description: >
  Expert guidance for implementing Clean Architecture in Android projects with Kotlin.
  Covers layer separation (Domain, Data, Presentation), dependency rule, Repository pattern,
  Use Cases (Interactors), and MVI presentation pattern. Use when designing features,
  organizing code structure, or implementing business logic separation.
license: Apache-2.0
metadata:
  author: spbrealty-android team
  version: "1.0.0"
---

# Clean Architecture for Android

## Overview

Clean Architecture separates code into layers with distinct responsibilities:
- **Presentation Layer**: UI and ViewModels (MVI pattern)
- **Domain Layer**: Business logic, Use Cases, Repository interfaces
- **Data Layer**: Repository implementations, data sources (API, Database)

## Layer Dependencies

```
Presentation Layer
       ↓ (depends on)
Domain Layer (no external dependencies)
       ↑ (depends on abstraction)
Data Layer
```

**Dependency Rule**: Dependencies point inward. Inner layers don't know about outer layers.

## Project Structure

```
app/
├── presentation/          # UI Layer
│   ├── feature1/
│   │   ├── Feature1ViewModel.kt
│   │   ├── Feature1Screen.kt
│   │   └── Feature1Contract.kt  # State & Events
│   └── common/            # Shared UI components
├── domain/                # Domain Layer (pure Kotlin)
│   ├── model/             # Domain models
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Use cases (interactors)
└── data/                  # Data Layer
    ├── repository/        # Repository implementations
    ├── remote/            # API, DTOs
    └── local/             # Database, DAOs
```

## Domain Layer

### Domain Models

```kotlin
// Pure business entities, no Android dependencies
data class Property(
    val id: PropertyId,
    val title: String,
    val price: Price,
    val location: Location
)

@JvmInline
value class PropertyId(val value: Long)

// Value objects with validation
data class Price(
    val amount: Double,
    val currency: Currency
) {
    init {
        require(amount >= 0) { "Price cannot be negative" }
    }
}
```

### Repository Interfaces

```kotlin
// Interface in Domain Layer
interface PropertyRepository {
    suspend fun getProperties(): List<Property>
    suspend fun getProperty(id: PropertyId): Property?
    fun observeProperties(): Flow<List<Property>>
}

// Use cases depend on abstractions, not implementations
class GetPropertiesUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(): Result<List<Property>> = 
        runCatching { repository.getProperties() }
}
```

### Use Cases (Interactors)

```kotlin
// Single responsibility use case
class SearchPropertiesUseCase(
    private val repository: PropertyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(
        query: String,
        filters: PropertyFilters
    ): Result<List<Property>> = withContext(dispatcher) {
        runCatching {
            repository.getProperties()
                .filter { it.matches(query, filters) }
                .sortedByDescending { it.relevanceScore }
        }
    }
}

// Use case can combine multiple repositories
class BookPropertyUseCase(
    private val propertyRepository: PropertyRepository,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        propertyId: PropertyId,
        bookingDates: DateRange
    ): Result<Booking> = runCatching {
        val user = userRepository.getCurrentUser()
            ?: throw UserNotAuthenticatedException()
        
        val property = propertyRepository.getProperty(propertyId)
            ?: throw PropertyNotFoundException()
        
        bookingRepository.createBooking(
            userId = user.id,
            propertyId = propertyId,
            dates = bookingDates
        )
    }
}
```

## Data Layer

### Repository Implementation

```kotlin
// Implementation in Data Layer
class PropertyRepositoryImpl(
    private val remoteDataSource: PropertyRemoteDataSource,
    private val localDataSource: PropertyLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : PropertyRepository {
    
    override suspend fun getProperties(): List<Property> {
        return if (networkMonitor.isOnline()) {
            remoteDataSource.getProperties()
                .also { localDataSource.saveProperties(it) }
                .map { it.toDomain() }
        } else {
            localDataSource.getProperties()
                .map { it.toDomain() }
        }
    }
    
    override fun observeProperties(): Flow<List<Property>> {
        return localDataSource.observeProperties()
            .map { list -> list.map { it.toDomain() } }
    }
}

// DTO to Domain mapper
fun PropertyDto.toDomain(): Property = Property(
    id = PropertyId(id),
    title = title,
    price = Price(amount = price, currency = Currency.RUB),
    location = Location(city, district)
)
```

### Remote Data Source

```kotlin
interface PropertyApi {
    @GET("properties")
    suspend fun getProperties(): List<PropertyDto>
    
    @GET("properties/{id}")
    suspend fun getProperty(@Path("id") id: Long): PropertyDto
}

class PropertyRemoteDataSource(
    private val api: PropertyApi
) {
    suspend fun getProperties(): List<PropertyDto> = 
        api.getProperties()
}
```

### Local Data Source

```kotlin
@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties")
    fun observeAll(): Flow<List<PropertyEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(properties: List<PropertyEntity>)
}

class PropertyLocalDataSource(
    private val dao: PropertyDao
) {
    fun observeProperties(): Flow<List<PropertyEntity>> = 
        dao.observeAll()
    
    suspend fun saveProperties(properties: List<PropertyEntity>) {
        dao.insertAll(properties)
    }
}
```

## Presentation Layer (MVI)

### Contract (State & Events)

```kotlin
// State - immutable data class
data class PropertyListState(
    val isLoading: Boolean = false,
    val properties: List<Property> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

// Events - sealed class for user actions
sealed class PropertyListEvent {
    data class SearchQueryChanged(val query: String) : PropertyListEvent()
    data class PropertyClicked(val property: Property) : PropertyListEvent()
    object Refresh : PropertyListEvent()
    object Retry : PropertyListEvent()
}

// Side effects - one-time events
sealed class PropertyListEffect {
    data class NavigateToDetail(val propertyId: PropertyId) : PropertyListEffect()
    data class ShowSnackbar(val message: String) : PropertyListEffect()
}
```

### ViewModel

```kotlin
class PropertyListViewModel(
    private val getProperties: GetPropertiesUseCase,
    private val searchProperties: SearchPropertiesUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(PropertyListState())
    val state: StateFlow<PropertyListState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<PropertyListEffect>()
    val effect: SharedFlow<PropertyListEffect> = _effect.asSharedFlow()
    
    init {
        loadProperties()
    }
    
    fun onEvent(event: PropertyListEvent) {
        when (event) {
            is PropertyListEvent.SearchQueryChanged -> onSearchQueryChanged(event.query)
            is PropertyListEvent.PropertyClicked -> onPropertyClicked(event.property)
            PropertyListEvent.Refresh -> loadProperties()
            PropertyListEvent.Retry -> loadProperties()
        }
    }
    
    private fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        
        viewModelScope.launch {
            searchProperties(query, filters = PropertyFilters())
                .onSuccess { properties ->
                    _state.update { 
                        it.copy(properties = properties, error = null) 
                    }
                }
        }
    }
    
    private fun onPropertyClicked(property: Property) {
        viewModelScope.launch {
            _effect.emit(PropertyListEffect.NavigateToDetail(property.id))
        }
    }
}
```

### Compose UI

```kotlin
@Composable
fun PropertyListScreen(
    viewModel: PropertyListViewModel,
    onNavigateToDetail: (PropertyId) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PropertyListEffect.NavigateToDetail -> 
                    onNavigateToDetail(effect.propertyId)
                is PropertyListEffect.ShowSnackbar -> 
                    /* Show snackbar */
            }
        }
    }
    
    PropertyListContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun PropertyListContent(
    state: PropertyListState,
    onEvent: (PropertyListEvent) -> Unit
) {
    Column {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(PropertyListEvent.SearchQueryChanged(it)) }
        )
        
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(
                message = state.error,
                onRetry = { onEvent(PropertyListEvent.Retry) }
            )
            else -> PropertyList(
                properties = state.properties,
                onPropertyClick = { onEvent(PropertyListEvent.PropertyClicked(it)) }
            )
        }
    }
}
```

## Dependency Injection (Koin)

```kotlin
// Domain Module
val domainModule = module {
    factory { GetPropertiesUseCase(get()) }
    factory { SearchPropertiesUseCase(get()) }
}

// Data Module
val dataModule = module {
    single<PropertyRepository> { PropertyRepositoryImpl(get(), get(), get()) }
    single { PropertyRemoteDataSource(get()) }
    single { PropertyLocalDataSource(get()) }
}

// Presentation Module
val presentationModule = module {
    viewModel { PropertyListViewModel(get(), get()) }
}
```

## Best Practices

### ✅ DO
- Keep domain layer pure (no Android/framework dependencies)
- Use immutable data classes for state
- Single Responsibility for Use Cases
- Map DTOs to Domain models immediately
- Handle errors at appropriate layer

### ❌ DON'T
- Expose DTOs to Presentation layer
- Use Android classes in Domain layer
- Create God Use Cases
- Skip error handling
- Mix data mapping with business logic

## Testing

### Domain Layer Testing
```kotlin
@Test
fun `use case returns mapped data`() = runTest {
    val mockRepository = mockk<PropertyRepository>()
    coEvery { mockRepository.getProperties() } returns listOf(mockProperty)
    
    val useCase = GetPropertiesUseCase(mockRepository)
    val result = useCase()
    
    assertTrue(result.isSuccess)
    assertEquals(listOf(mockProperty), result.getOrNull())
}
```

### ViewModel Testing
```kotlin
@Test
fun `view model emits loading then success`() = runTest {
    val mockUseCase = mockk<GetPropertiesUseCase>()
    coEvery { mockUseCase() } returns Result.success(emptyList())
    
    val viewModel = PropertyListViewModel(mockUseCase, mockk())
    
    viewModel.state.test {
        assertEquals(PropertyListState(isLoading = false), awaitItem())
        viewModel.onEvent(PropertyListEvent.Refresh)
        assertEquals(PropertyListState(isLoading = true), awaitItem())
        assertEquals(PropertyListState(isLoading = false, properties = emptyList()), awaitItem())
    }
}
```
