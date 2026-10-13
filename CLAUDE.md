# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

NoConnect is an Android off-grid, end-to-end encrypted messenger. It is a GPLv3 fork of Briar 1.5.21 (snapshot import, commit `5321d60`), with all upstream branding removed. It is deliberately **not wire-compatible** with upstream: protocol labels, ClientIds and TransportIds use the `org.chagolchana.*` namespace.

Two layers:
- **nc** ("chagol", infrastructure): `nc-api`, `nc-core`, `nc-android`, `nc-java`. Package `org.chagolchana.chagol`, classes `Chagol*`.
- **noconnect** (app): `noconnect-api`, `noconnect-core`, `noconnect-android`, `noconnect-headless`. Package `org.chagolchana.noconnect`, classes `NoConnect*`.
- `nc-lib-*`: support libraries vendored from upstream releases on Maven Central (null-safety, SOCKS socket, jtorctl, onionwrapper core/java/android, dont-kill-me). Packages `org.chagolchana.{nullsafety,socks,util,onionwrapper,dontkillme}`; jtorctl keeps `net.freehaven.tor.control`.

Upstream Briar specs and history remain the best reference for how a subsystem works (bramble = nc, briar = noconnect). Use `tools/rename-upstream.py` to port upstream changes: apply them to an upstream-named copy, then run the script.

## Naming rules

- Never reintroduce Briar/Bramble names in code, resources, strings or protocol identifiers. Attribution belongs only in `LICENSE`, `NOTICE`, `README.md`, `docs/`, and `tools/rename-upstream.py`.
- The only upstream names left in build files are the prebuilt binary coordinates `org.briarproject:tor-*` and `org.briarproject:lyrebird-*`.
- Resources use `nc_*` (colours, layouts, strings) and `NoConnect*` (styles). URI scheme `noconnect://`.

## Build and test

Requires JDK 17 and Android SDK platform 35 / build tools 35.0.0 (`local.properties` with `sdk.dir`).

```
./gradlew :noconnect-android:assembleOfficialDebug         # APK (flavours: official, screenshot)
./gradlew :nc-core:test                                    # one module's JVM tests
./gradlew :nc-core:test --tests '*TransportKeyManagerImplTest'
./gradlew :noconnect-android:testOfficialDebugUnitTest      # Robolectric/unit tests
./gradlew :noconnect-android:lintOfficialDebug
```

- **Dependency pinning:** every non-`nc-lib` module applies the `witness` plugin with SHA-256 pins in `witness.gradle`. A dependency change that isn't re-pinned fails the build ("No dependency for integrity assertion"). Regenerate with `./update-dependency-pinning.sh`. Compare new pins against the old ones before committing.
- **Known-flaky tests:** the `*IntegrationTest` classes in `nc-core` and `noconnect-core` time out under load. They fail intermittently on pristine upstream too. Re-run a failing class on its own before treating it as a regression.
- When verifying a change, confirm the relevant compile task actually executed (not `UP-TO-DATE` or skipped) before claiming the build passes.
- The headless module downloads desktop Tor/lyrebird binaries during configuration. On networks that block executables, exclude it from `settings.gradle` locally.

## Architecture

- Each subsystem is an interface in an `*-api` module, an `*Impl` in a `*-core` module, and a Dagger module. Clients (messaging, forums, blogs, private groups, introductions, sharing) register validators and hooks when constructed, so each module has an `EagerSingletons` class listed in `ChagolCoreEagerSingletons` / `NoConnectCoreEagerSingletons`.
- **Root DI graph:** `noconnect-android/.../android/AndroidComponent.java` combines `ChagolCoreModule`, `NoConnectCoreModule`, `ChagolAndroidModule`, `AppModule` and feature modules (attachments, mailbox, removable drive…). Adding a client means wiring its module into the core module and its eager singletons into the matching interface.
- **Sync:** messages are stored in the encrypted H2 database, offered to contacts who share the group, and exchanged over BTP streams. Streams start with pseudorandom tags derived from per-contact rotating transport keys. Peers that aren't contacts are rejected. Group content (forums, blogs, private groups) is re-shared member to member; private messages are not.
- **Transports** are plugins (`nc-core/.../plugin`, platform ones in `nc-android` / `nc-java`): Bluetooth, LAN TCP, Tor (via `nc-lib-onionwrapper-*`), removable drives, Mailbox.
- **Android UI** uses ViewModels (`viewmodel/ViewModelFactory`, `@ViewModelKey` multibindings in per-feature Dagger modules) plus `NoConnectService` (foreground service owning the lifecycle).
- **Crash reports** are never uploaded. The report screen builds the report and opens the Android share sheet (`reporting/ReportViewModel`).

## Docs

- `docs/HANDOFF.md`: **start here**. Current state, next steps in priority order, and this machine's build quirks.

- `docs/architecture/2026-10-12-architecture-review-and-roadmap.md`: findings and roadmap.
- `docs/superpowers/specs/2026-10-12-rebase-and-roadmap-execution.md`: the milestone plan (M1–M6) currently being executed.
