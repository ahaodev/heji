# Copilot instructions

## Build, test, and lint commands

The current checkout is a root-level Android project with a single module, `:app`.

- Run the full local unit-test suite: `./gradlew --no-daemon testDebugUnitTest`
- Run one unit-test class: `./gradlew --no-daemon testDebugUnitTest --tests 'com.hao.heji.data.converters.MoneyConvertersTest'`
- Run one unit-test method: `./gradlew --no-daemon testDebugUnitTest --tests 'com.hao.heji.data.converters.MoneyConvertersTest.testToLong'`
- Run Android lint: `./gradlew --no-daemon lintDebug`
- Build a debug APK: `./gradlew --no-daemon :app:assembleDebug`
- Run instrumentation tests on a connected device/emulator: `./gradlew --no-daemon connectedDebugAndroidTest`
- Run one instrumentation-test class: `./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hao.heji.data.db.BookTest`
- Run one instrumentation-test method: `./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hao.heji.data.db.BookTest#myTest`

Unit tests live under `app/src/test`, and instrumentation/device tests live under `app/src/androidTest`.

`app/build.gradle.kts` reads required configuration during Gradle setup, so test/lint/build commands all assume these properties already exist. `local.properties` must define `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. `gradle.properties` must define `LOCALHOST` and `HJSERVER`. The checked-in GitHub Actions workflows still reference older `client/` and `server/` directories; for this checkout, run Gradle from the repository root instead.

## High-level architecture

- **App bootstrap and routing:** `App` initializes MMKV, Sentry, the per-user Room database, and Koin. `StartupActivity` decides whether to enter `MainActivity` directly or send the user through login based on offline mode and whether a stored token exists.
- **Global config and session state:** `Config` is a thin facade over `DataStoreManager` (MMKV-backed storage) for the current user, selected book, server URL, MQTT broker URL, and last sync timestamp. `AppViewModel` exposes global `configChange` and `loginEvent` flows that activities/services observe.
- **UI shape:** the signed-in shell is a single-activity, multi-fragment app: `MainActivity` owns the Navigation drawer and `NavHostFragment`, while startup/auth still use separate `StartupActivity` and `LoginActivity` entry points. Feature screens live under `ui/...` with Koin-provided ViewModels from `di/AppModule.kt`.
- **Persistence model:** Room entities live under `data/db` (`Book`, `Bill`, `Category`, `Image`, `BookUser`). The active database file is user-scoped by user ID (effectively `${userId}.db`), and `App.switchDataBase()` swaps the singleton database when the active user changes.
- **Sync model:** `docs/sync-v2.md` is the authoritative sync design. The app is local-first: CRUD writes land in Room first, then `SyncService`/`SyncTrigger` pull server changes over HTTP and push unsynced local changes over HTTP. MQTT is only the downstream notification channel; it is not the source of truth for uploads.

## Key conventions

- `Config.book` is only safe after `MainViewModel.switchModelAndBook()` has run. In startup or transitional code, use `Config.bookOrNull` instead of touching `Config.book` directly.
- Most feature code reads and writes Room through the global `App.dataBase` singleton, even inside ViewModels and some repository/helper classes. Reuse that pattern unless you are intentionally refactoring a wider slice of the app.
- Sync state is part of the data model. New or edited local records should preserve the `synced` / `deleted` flags and follow the local-first flow from `docs/sync-v2.md`; many delete paths intentionally use soft-delete / `preDelete(...)` first and only hard-delete after sync or remote reconciliation.
- IDs are generated client-side with `Xid.string()` for core synced entities such as `Book` and `Bill`. Preserve that behavior for any new synced records.
- Creating a new book is expected to seed default categories from `BookType` immediately (`MainViewModel.createDefaultBook()` and `BookViewModel.insertDefaultCategories()` show the pattern).
- `AuthorizedInterceptor` adds the bearer token to every request except login/register and emits a global login event on HTTP 401. Authentication failures are handled centrally through that event flow rather than ad hoc screen-level redirects.
