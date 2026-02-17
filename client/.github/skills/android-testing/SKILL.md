---
name: android-testing
description: Comprehensive testing strategy involving Unit, Integration, Koin, and Screenshot tests.
---

# Android Testing Strategies

This skill provides expert guidance on testing modern Android applications, inspired by "Now in Android". It covers **Unit Tests**, **Koin Integration Tests**, and **Screenshot Testing**.

## Testing Pyramid

1.  **Unit Tests**: Fast, isolate logic (ViewModels, Repositories).
2.  **Integration Tests**: Test interactions (Room DAOs, Retrofit vs MockWebServer).
3.  **UI/Screenshot Tests**: Verify UI correctness (Compose).

## Dependencies (`libs.versions.toml`)

Ensure you have the right testing dependencies.

```toml
[libraries]
junit4 = { module = "junit:junit", version = "4.13.2" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version = "1.1.5" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version = "3.5.1" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
koin-test = { group = "io.insert-koin", name = "koin-test", version.ref = "koin" }
koin-test-junit4 = { group = "io.insert-koin", name = "koin-test-junit4", version.ref = "koin" }
roborazzi = { group = "io.github.takahirom.roborazzi", name = "roborazzi", version.ref = "roborazzi" }
```

## Screenshot Testing with Roborazzi

Screenshot tests ensure your UI doesn't regress visually. NiA uses **Roborazzi** because it runs on the JVM (fast) without needing an emulator.

### Setup

1.  Add the plugin to `libs.versions.toml`:
    ```toml
    [plugins]
    roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
    ```
2.  Apply it in your module's `build.gradle.kts`:
    ```kotlin
    plugins {
        alias(libs.plugins.roborazzi)
    }
    ```

### Writing a Screenshot Test

```kotlin
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel5)
class MyScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureMyScreen() {
        composeTestRule.setContent {
            MyTheme {
                MyScreen()
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage()
    }
}
```

## Koin Testing

Use Koin's test utilities to override and inject dependencies in tests.

```kotlin
class MyDaoTest : KoinTest {

    private val database: MyDatabase by inject()
    private lateinit var dao: MyDao

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(testModule)
    }

    @Before
    fun init() {
        dao = database.myDao()
    }
    
    // ... tests
}
```

## Running Tests

*   **Unit**: `./gradlew test`
*   **Screenshots**: `./gradlew recordRoborazziDebug` (to record) / `./gradlew verifyRoborazziDebug` (to verify)
