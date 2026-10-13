# NoConnect: session handoff

Last updated: 2026-10-13. Read this first, then `CLAUDE.md`. This file records what has been done, the state of the repo and this machine, and what to do next, in priority order.

## 1. Where things are

- **Branch:** `rebase/upstream-1.5.21`. Not pushed; nothing has been merged to `master`. History: `master` (2017 fork) → `rename/remove-briar-branding` (2017 code renamed; superseded) → `rebase/upstream-1.5.21` (current work).
- **Remote:** `origin` = https://github.com/rabimba/noconnect.git.
- **Working tree:** clean as of this file's commit, apart from the untracked `.claude/` folder (agent worktrees; don't commit it).
- **Key docs:**
  - `CLAUDE.md`: architecture, naming rules, build and test commands
  - `docs/architecture/2026-10-12-architecture-review-and-roadmap.md`: the original review, findings F1–F16, roadmap
  - `docs/superpowers/specs/2026-10-12-rebase-and-roadmap-execution.md`: milestones M1–M6 and their status table
  - `docs/design/ble-discovery.md`: the BLE transport, as built
  - `docs/design/experimental-crypto-and-relay.md`: hybrid PQ, sender keys, relay threat model
  - `docs/platform/android-17-local-network.md`: what changes at target SDK 37
  - `tools/rename-upstream.py`: maps upstream Briar names to NoConnect; use it to port upstream fixes

## 2. Decisions already made by the user (don't reopen without asking)

- **Goal:** a real-world product for disasters, protests and places with no connectivity.
- **Base:** rebased on upstream Briar 1.5.21 as a snapshot import. Briar has been in maintenance mode since July 2026.
- **Names:**
  - Bramble → **Chagol** (`org.chagolchana.chagol`, modules `nc-*`)
  - Briar → **NoConnect** (`org.chagolchana.noconnect`, modules `noconnect-*`)
  - Protocol strings were renamed too, so NoConnect is wire-incompatible with Briar (accepted).
- **Briar's helper libraries** are vendored as `nc-lib-*` and renamed. The only upstream names left in the code are the prebuilt Tor/lyrebird Maven coordinates `org.briarproject:tor-*` / `lyrebird-*`. Attribution lives only in `LICENSE`, `NOTICE`, `README.md`, `docs/` and the rename tool. CI enforces this.
- **Tor stays. RSS feeds are gone.** Crash reports are never uploaded; the user shares them through the share sheet.
- **New crypto and the relay** sit behind `FeatureFlags` and stay **off** until reviewed and audited. The BLE transport is on in debug builds only.

## 3. What's done (all committed)

M1 Import and rename:
- Upstream snapshot imported; 7 libraries vendored; scripted rename.
- New logo and teal palette (replacing upstream's lime/navy).
- Crash-report upload removed; RSS removed.
- `README.md`, `NOTICE` and `CLAUDE.md` rewritten.

M2 Platform:
- Target/compile SDK 36. Edge-to-edge insets handled in `BaseActivity`. `enableOnBackInvokedCallback="false"` keeps `onBackPressed()` working.
- GitHub Actions CI (`.github/workflows/ci.yml`).
- Android 17 local-network audit written.

M3 Security:
- Argon2id password KDF with migration from scrypt, plus cost bounds (`Argon2idKdf`, `CryptoComponentImpl`).
- Entropy-based password policy, requiring 45 bits or more.
- `KeyStrengthener.destroyKey()` called on account deletion.
- Constant-time key-agreement confirmation check.
- Parser robustness tests (`*RobustnessTest`).

M4 Features:
- Build-age update reminder (`BuildAgePolicy`).
- Install update from file (`update/InstallUpdateActivity`, `UpdateCheck`), verified end to end on the emulator.
- "Check in with everyone" (`contact/CheckInSender`).

M5 Bluetooth LE (debug builds only):
- `nc-android/.../plugin/ble/AndroidBlePlugin` with factory and connection classes; `BleRendezvousTokens` and `BleAdvertisementData` in `nc-core`.
- **Verified on two Android 16 emulators:** discovery, L2CAP connection, BTP contact recognition, and a private message delivered over BLE.

M6 Experimental (all off):
- Three feature flags, off everywhere.
- `HybridKem` (X25519 + ML-KEM-768, X-Wing combiner), tested but not wired into any protocol.
- Sender keys and relay are design only.

Debug harness: `noconnect-android/src/debug/.../test/TestContactReceiver` makes two emulators contacts without QR codes. Its docstring has the adb commands.

Test status at the last full run: 2,263 unit tests, 233 skipped, 37 failing. All 37 are environmental (see section 5).

## 4. Next steps, in priority order

1. **BLE on real phones.** Requires two physical Android 10+ phones from different manufacturers.
   - Measure discovery latency, battery use per hour, and behaviour under Doze and manufacturer battery killers.
   - Then decide whether to turn on `shouldEnableBleTransport()` in release builds (`AppModule`).
2. **BLE follow-ups that need no devices:**
   - Rotate the BLE rendezvous secret when a contact is removed (`docs/design/ble-discovery.md`, open items).
   - Unit-test `AndroidBlePlugin` logic, for example by extracting the sighting and matching logic into a plain-Java class.
   - Add BLE to the Transports screen (`navdrawer/TransportsActivity`) so users can see and toggle it.
   - Request the Nearby-devices permissions when BLE is enabled, not only during "add contact nearby".
3. **Predictive back.** Migrate the roughly 20 `onBackPressed()` overrides to `OnBackPressedDispatcher`, then remove `android:enableOnBackInvokedCallback="false"` from the manifest.
4. **Translations.** These strings exist in English only: `update_*`, `check_in_*`, `update_reminder`, `dev_report_share_subject`. Set up a NoConnect Transifex or Weblate project; the slug is in `noconnect-android/.tx/config`.
5. **Hybrid PQ in BQP** (flag off). Follow `docs/design/experimental-crypto-and-relay.md` §1: new BQP version, encapsulation after the X25519 exchange, transcript-bound KDF. A Tamarin model and an external audit are needed before enabling it.
6. **Group sender keys** (§2), then the **sealed-envelope relay** (§3). The relay needs a threat-model review and a simulation in The ONE or ns-3 first.
7. **Deferred infrastructure:**
   - Replace witness pins with Gradle `verification-metadata.xml`. This needs a network that can download all Tor binaries, so do it in CI.
   - Zeroise secrets in memory (low priority).
8. **Distribution:**
   - Register as a verified Android developer.
   - Set up F-Droid reproducible builds.
   - Decide whether a Google Play build should drop `REQUEST_INSTALL_PACKAGES` (install update from file).
9. **Before merging to master:** open a PR from `rebase/upstream-1.5.21`. Let CI run, which is the first full build including `noconnect-headless`. Review the debug-only harness and confirm it isn't in release APKs (`src/debug` only).

## 5. This machine: build quirks

- **Network:** slow (~340 KB/s), behind a proxy that blocks:
  - the Windows/macOS Tor and lyrebird jars
  - the Robolectric runtime
  - Tor itself (TLS handshakes fail)
  - git over HTTPS to code.briarproject.org (use the github.com/briar/briar mirror)
- **Gradle:** the wrapper download times out. A verified Gradle 8.14.3 was unpacked at `/tmp/gradle-dl/gradle-8.14.3/bin/gradle`. `/tmp` can be wiped; re-download with `curl -C -` and check `gradle-8.14.3-bin.zip.sha256`.
- **Headless module:** to build locally, remove `include ':noconnect-headless'` from `settings.gradle` temporarily. The helper script `/tmp/nc-rename/build.sh <log> <tasks…>` did this and restored the file afterwards; it may be gone. To compile headless, also comment out its `windows`/`macos` binary dependencies **and** their pins in `noconnect-headless/witness.gradle`, then restore both files.
- **Dependency pins:** regenerate with `update-dependency-pinning.sh`, or the per-module `calculateChecksums` task, excluding headless. Compare the new hashes against the old ones before committing (so far: 739/739 identical).
- **Expected test failures here:**
  - `*IntegrationTest` timeouts, which also fail on pristine upstream; re-run a class on its own to confirm
  - Robolectric download (`SetupActivityTest`, `ChangePasswordActivityTest`)
  - `LanTcpPluginTest` sockets
- **Emulators:** AVDs `Medium_Phone_API_36.1` (emulator-5554) and `NC_B` (emulator-5556), both Android 16 arm64. Boot headless with `emulator -avd NAME -no-window -no-audio -no-snapshot -gpu swiftshader_indirect [-port 5556]`. Their test accounts use the password `Correct-Horse-Battery-9` ("Tester2" and "Bob"), and the two are already BLE contacts. A version-bumped test build was installed earlier, so reinstall with `adb install -r -d`.
- **Shell:** zsh mangles `$var:x` (path modifiers). Use `${var}:x` or write bash scripts.

## 6. Lessons from this work

- Confirm the relevant compile or test task actually **executed** (not `UP-TO-DATE` or skipped) before claiming a build passes. One RSS-removal "pass" was false.
- Never delete lines from Android `strings.xml` line by line; translated strings can span several lines. Use a parser-aware edit, then run `xmllint` on every file.
- macOS filenames are case-insensitive: deleting upstream `README.md` also deleted our `Readme.MD`.
- Animal Sniffer enforces the old API level in `nc-*` cores (for example, no `Math.floorDiv`). Run `:nc-core:animalsnifferMain`.
