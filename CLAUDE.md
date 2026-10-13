# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

NoConnect is an Android app for off-the-grid, encrypted peer-to-peer messaging over Wi-Fi/Bluetooth. It is a GPLv3 fork of Briar 0.15 (mid-2017), with all upstream branding removed. There are two layers:

- **nc / "chagol"** (infrastructure): `nc-*` modules, package `org.chagolchana.chagol`, classes prefixed `Chagol*` (e.g. `ChagolCoreModule`)
- **noconnect** (app): `noconnect-*` modules, package `org.chagolchana.noconnect`, classes prefixed `NoConnect*` (e.g. `NoConnectCoreModule`, `NoConnectService`)

Upstream Briar's docs and history are still the best reference for how a subsystem works (its "bramble" layer corresponds to nc, its "briar" layer to noconnect). Don't reintroduce Briar/Bramble names in code, resources or protocol strings. Attribution belongs only in `LICENSE`, `NOTICE` and the README. Protocol identifiers (`ClientId`, `TransportId`, crypto/KDF labels) use the `org.chagolchana.chagol.*` / `org.chagolchana.noconnect.*` namespaces. Changing them breaks wire and database compatibility. Resources use `nc_*` (colours, layouts) and `NoConnect*` (styles).

## Build

Toolchain is old: Android Gradle Plugin 2.3.2, Gradle wrapper 4.1-milestone-1, `compileSdkVersion 23`, build tools 25.0.3, `jcenter()` repositories. The README says to build from Android Studio; the last upstream change made it build on Java 8.

```
./gradlew :noconnect-android:assembleDebug      # build the APK
./gradlew :noconnect-android:lint
./gradlew :nc-core:test                         # per-module JVM tests (test task exists per module)
./gradlew :nc-core:test --tests '<fully.qualified.TestClass>'
```

Build notes:
- `local.properties` (Android SDK path) is gitignored and must exist locally.
- `nc-android` `preBuild` depends on tasks that download Tor binaries and GeoIP data from `briarproject.org/build/` into `nc-android/src/main/res/raw/` and verify their SHA-256 checksums. The Android build fails if those downloads fail. These URLs are the only upstream references left in the build.
- Every module uses **gradle-witness** (`libs/gradle-witness.jar`): `dependencyVerification { verify = [...] }` pins SHA-256 hashes of dependencies. Adding or bumping a dependency requires updating its hash in that module's `build.gradle`, or the build fails.
- Prebuilt patched jars live in `nc-android/libs` (jtorctl) and `nc-j2se/libs` (bluecove, jssc, jnotify plus native libs); the patches used to build them are in `patches/`.
- The debug build type also runs ProGuard (`minifyEnabled true`), so new reflection-based code may need `noconnect-android/proguard-rules.txt` entries.

There are currently no `src/test` directories in the repo, even though modules declare test dependencies (JUnit 4, jMock, Robolectric, Mockito). The `testOutput` configuration / `jarTest` task in `nc-api` and `nc-core` exist so downstream modules can reuse their test helpers.

## Architecture

Module dependency graph (lower layers are pure Java, mostly Java 1.6 source level; avoid newer language features there):

```
nc-api  ←  nc-core  ←  nc-android (Android lib: Tor, Bluetooth, LAN TCP plugins)
   ↑          ↑     ←  nc-j2se    (desktop plugins: bluetooth, modem, file; not used by the app)
noconnect-api ← noconnect-core
                           ↖
             noconnect-android (the app; depends on noconnect-core + nc-android)
```

- **`*-api` modules** hold interfaces, events, constants, and value types. **`*-core` modules** hold the `*Impl` classes plus a Dagger module per subsystem. Code depends on API interfaces; implementations are bound via Dagger.
- **nc layer** is the transport-agnostic infrastructure: crypto (spongycastle), H2-backed encrypted DB (`db/H2Database`, `JdbcDatabase`, `DatabaseComponentImpl`), sync protocol, transport key management, BDF data encoding, event bus, lifecycle, contact exchange / key agreement (QR-code based), and the plugin system (`plugin/PluginManagerImpl`, `Poller`, `ConnectionManagerImpl`) that platform modules supply transport plugins to.
- **noconnect layer** implements user-facing clients on top of sync: messaging, forums, blogs, RSS feeds, private groups, introductions, and sharing. Each client is a sync "client": it owns groups and messages, validates incoming messages through a validator, and hooks into the DB via incoming-message hooks.
- **Dependency injection**: Dagger 2.0.2. The root graph is `noconnect-android/.../android/AndroidComponent.java`, which combines `ChagolCoreModule`, `NoConnectCoreModule`, `ChagolAndroidModule`, and `AppModule`. `*EagerSingletons` interfaces list objects that must be instantiated at startup (validators/hooks register themselves when constructed). Activity-scoped injection goes through `activity/ActivityComponent`. When you add a new manager or client, wire it into the right `*Module` and, if needed, into the eager-singleton interface or `AndroidComponent`'s exposed methods.
- **Threading**: work runs on qualified executors (`@DatabaseExecutor`, `@CryptoExecutor`, `@IoExecutor`, `AndroidExecutor` for the UI thread). DB transactions must run on the DB executor. UI code typically goes through controller classes (`android/controller`, and per-feature `*ControllerImpl`) rather than calling managers directly.
- **App lifecycle**: `NoConnectService` (foreground service) owns the `LifecycleManager` startup/shutdown. `splash/SplashScreenActivity` routes to setup or `NavDrawerActivity`. The time-based expiry lock (`EXPIRY_DATE` → `ExpiredActivity`) has been disabled locally; see commits `4e89235` and `35bc4f8`.
- `TestingConstants` ties debug behaviour to `BuildConfig.DEBUG` (log level; screenshots are blocked in release builds).
- `org.thoughtcrime.securesms` (emoji and UI components, from Signal) and `im.delight.android.identicons` are vendored third-party code inside `noconnect-android`.

## Repo hygiene

`build/`, `.gradle/`, `.idea/`, and `*.iml` are in `.gitignore`, but many of those files were committed before the ignore rules were added. Don't stage changes to them.
