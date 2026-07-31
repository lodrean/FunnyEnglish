# Template: Creating New Feature Module

This template helps you create a new feature module following the modular architecture.

## Step 1: Add Feature to Enum

```kotlin
// core/src/commonMain/kotlin/com/funnyenglish/core/toggle/Feature.kt

enum class Feature(...) {
    // ... existing features
    
    MY_FEATURE("myfeature.key", false, "Description of my feature", true)
}
```

## Step 2: Create Module

### File: settings.gradle.kts
```kotlin
include(":feature-my")
```

### File: feature-my/build.gradle.kts
```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeatureMy"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Feature API
            api(project(":feature-api"))
            
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            
            // ViewModel
            api(libs.lifecycle.viewmodel.compose)
            api(libs.koin.compose.viewmodel)
        }
    }
}

android {
    namespace = "com.funnyenglish.feature.my"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

## Step 3: Create Feature Entry

### File: feature-my/src/commonMain/kotlin/com/funnyenglish/feature/my/MyFeatureEntry.kt

```kotlin
package com.funnyenglish.feature.my

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funnyenglish.core.toggle.Feature
import com.funnyenglish.featureapi.api.FeatureEntry

class MyFeatureEntry : FeatureEntry {
    override val feature: Feature = Feature.MY_FEATURE
    override val navigationOrder: Int = 10
    override val navigationLabel: String = "Моя фича"
    
    override val navigationIcon: @Composable () -> Unit = {
        Icon(Icons.Default.Star, contentDescription = "Моя фича")
    }
    
    @Composable
    override fun Content(
        modifier: Modifier,
        onNavigate: (Any) -> Unit
    ) {
        MyScreen(onNavigate = onNavigate)
    }
}
```

### File: feature-my/src/commonMain/kotlin/com/funnyenglish/feature/my/MyScreen.kt

```kotlin
package com.funnyenglish.feature.my

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyScreen(
    onNavigate: (Any) -> Unit,
    viewModel: MyViewModel = koinViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моя фича") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("My Feature Content")
        }
    }
}
```

### File: feature-my/src/commonMain/kotlin/com/funnyenglish/feature/my/MyViewModel.kt

```kotlin
package com.funnyenglish.feature.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load data
        }
    }
}

data class MyState(
    val isLoading: Boolean = false,
    val data: List<String> = emptyList()
)
```

### File: feature-my/src/commonMain/kotlin/com/funnyenglish/feature/my/di/MyModule.kt

```kotlin
package com.funnyenglish.feature.my.di

import com.funnyenglish.feature.my.MyViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val myModule = module {
    viewModelOf(::MyViewModel)
}
```

## Step 4: Register in App Module

### File: app/src/.../App.kt

```kotlin
import com.funnyenglish.feature.my.MyFeatureEntry
import com.funnyenglish.feature.my.di.myModule

// In Koin modules list
val appModules = listOf(
    coreModule,
    featureApiModule,
    myModule,  // Add feature module
    // ... other modules
)

// In feature registration
val featureEntries = listOf(
    HomeFeatureEntry(),
    MyFeatureEntry(),  // Add feature entry
    // ... other features
).filter { toggleManager.isEnabled(it.feature) }
```

## Step 5: Add Conditional UI

### In another feature module

```kotlin
@Composable
fun SomeOtherScreen() {
    val toggleManager = koinInject<FeatureToggleManager>()
    
    Column {
        // Always visible content
        
        // Conditional content
        if (toggleManager.isEnabled(Feature.MY_FEATURE)) {
            Button(onClick = { /* navigate to my feature */ }) {
                Text("Open My Feature")
            }
        }
    }
}
```

## Step 6: Backend Toggle (Optional)

### File: backend/.../controller/FeatureToggleController.kt

```kotlin
@RestController
@RequestMapping("/api/features")
class FeatureToggleController {
    
    @GetMapping("/toggles")
    fun getFeatureToggles(
        @AuthenticationPrincipal user: UserPrincipal
    ): Map<String, Boolean> {
        return mapOf(
            "myfeature.key" to shouldEnableForUser(user)
        )
    }
    
    private fun shouldEnableForUser(user: UserPrincipal): Boolean {
        // A/B testing logic
        // Percentage rollout
        // Beta users check
        return false
    }
}
```

## Step 7: QA Checklist

- [ ] Feature compiles successfully
- [ ] Feature appears in UI when enabled
- [ ] Feature hidden when disabled
- [ ] Navigation works correctly
- [ ] Works on all platforms (Android, Desktop, iOS)
- [ ] No crashes when toggling at runtime
- [ ] Analytics events tracked

## Migration from Legacy Module

If migrating from `composeApp` or `shared`:

1. Copy code from old location
2. Update imports to use `core` and `feature-api`
3. Extract ViewModel to feature module
4. Remove from old module
5. Test thoroughly
