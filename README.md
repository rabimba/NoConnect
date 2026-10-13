# NoConnect

Secure messaging that keeps working when the internet doesn't.

NoConnect is an Android messenger for people who can't count on a working network: during disasters, protests, shutdowns, or in places with no coverage. Messages are end-to-end encrypted and travel directly between devices over Bluetooth, Wi-Fi or removable drives, or over Tor when the internet is available.

> **Status: experimental.** NoConnect is a fork of Briar 1.5.21 and is not wire-compatible with Briar. It has not had an independent security audit; don't rely on it where your safety depends on it until it has.

## What it does

- **Direct, contact-to-contact sync.** You add contacts by scanning each other's QR codes in person, or by exchanging links. Messages sync whenever two contacts' devices can reach each other.
- **Several transports:** Bluetooth, Wi-Fi (same network or hotspot), Tor onion services, and removable drives (SD card or USB stick) for sneakernet.
- **Private messages, forums, blogs and private groups.** Group posts spread from member to member, so they reach people you aren't directly in range of, as long as a chain of members connects you.
- **Offline app sharing.** Share the app itself with a nearby phone over a local hotspot.
- **Mailbox support.** An always-on device can hold messages for you while your phone is offline.
- **Panic button.** Integrates with panic-trigger apps to lock or wipe NoConnect.

What it does **not** do (yet): there is no discovery of strangers and no multi-hop routing between people who aren't contacts or fellow group members. See the [roadmap](docs/architecture/2026-10-12-architecture-review-and-roadmap.md).

## Building

Requirements: JDK 17 and the Android SDK (platform 35, build tools 35.0.0).

```
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties   # path to your SDK
./gradlew :noconnect-android:assembleOfficialDebug               # debug APK
./gradlew test                                                   # JVM unit tests
./gradlew :noconnect-android:testOfficialDebugUnitTest           # Android unit tests
```

The APK is written to `noconnect-android/build/outputs/apk/official/debug/`.

Dependencies are pinned by SHA-256 in each module's `witness.gradle`. After changing a dependency, run `./update-dependency-pinning.sh`.

## Project layout

| Module | Contents |
|---|---|
| `nc-api`, `nc-core` | Transport-agnostic core: crypto, encrypted database, sync protocol, transport keys, contact exchange, plugins |
| `nc-android`, `nc-java` | Platform transports (Bluetooth, LAN, Tor) for Android and desktop JVM |
| `noconnect-api`, `noconnect-core` | Messaging, forums, blogs, private groups, introductions, sharing |
| `noconnect-android` | The Android app |
| `noconnect-headless` | Headless JVM client with a REST API |
| `nc-lib-*` | Support libraries vendored from upstream releases (null-safety, SOCKS, Tor control, onion wrapper, battery-optimisation helpers) |

## Licence

NoConnect is licensed under the [GNU General Public License v3](LICENSE). It is derived from [Briar](https://briarproject.org) (GPLv3, The Briar Project) and includes other third-party code; see [NOTICE](NOTICE).
