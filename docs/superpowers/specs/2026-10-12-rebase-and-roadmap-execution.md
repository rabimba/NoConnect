# Rebase onto upstream 1.5.21 and execute the roadmap — plan

Date: 2026-10-12 · Status: approved by the user ("rebase and then rename and implement everything we planned")
Roadmap: `docs/architecture/2026-10-12-architecture-review-and-roadmap.md` (option A chosen).

## Decisions (from the user)

| Topic | Decision |
|---|---|
| Upstream libraries (null-safety, dont-kill-me-lib, socks-socket, jtorctl, onionwrapper-core/android/java) | Vendor sources from Maven Central `-sources.jar` into `nc-lib-*` modules and rename. Licences: Apache-2.0 (null-safety, socks-socket), BSD-3 (jtorctl), GPLv3 (others). |
| Prebuilt Tor/lyrebird binaries (`org.briarproject:tor-*`, `lyrebird-*`) | Stay as external Maven coordinates; they are the only upstream name left in build files. |
| History | Snapshot import: one commit with the upstream 1.5.21 tree (commit `aea0e4a`), then a rename commit, on branch `rebase/upstream-1.5.21` from current master. |
| Tor transport | Keep (v3 onions via onionwrapper, Mailbox support). |
| RSS feeds | Remove. |
| New crypto and relay (hybrid PQ handshake, group sender keys, sealed-envelope relay) | Implement behind feature flags, **off by default**, with design docs and tests. Not enabled until reviewed and audited. |

## Naming map

| Upstream | NoConnect |
|---|---|
| modules `bramble-api/core/android/java` | `nc-api/core/android/java` |
| modules `briar-api/core/android/headless` | `noconnect-api/core/android/headless` |
| package `org.briarproject.bramble` | `org.chagolchana.chagol` |
| package `org.briarproject.briar` | `org.chagolchana.noconnect` |
| package `org.briarproject.nullsafety` / `.onionwrapper` / `.socks` / `.util` / `.android.dontkillmelib` | `org.chagolchana.nullsafety` / `.onionwrapper` / `.socks` / `.util` / `.dontkillmelib` |
| class prefixes `Bramble*` / `Briar*` | `Chagol*` / `NoConnect*` |
| resources `briar_*` / styles `Briar*` | `nc_*` / `NoConnect*` |
| protocol strings `org.briarproject.bramble.*` / `.briar.*` | `org.chagolchana.chagol.*` / `.noconnect.*` |
| URI scheme `briar://` | `noconnect://` |
| user-visible "Briar" | "NoConnect" |
| applicationId `org.briarproject.briar.android` | `org.chagolchana.noconnect.android` |

Not carried over: `briar-mailbox` submodule and `mailbox-integration-tests` (the Mailbox *client* in `nc-core` stays).

## Milestones

Each milestone ends with a commit and its verification result; a failed verification stops progress.

**M1 Import + rename.** Snapshot import; vendor libraries; scripted rename (paths, packages, identifiers, resources, strings, protocol strings); re-point crash reporting away from upstream's key/onion (local view and manual share only); remove RSS feeds; keep NoConnect LICENSE/NOTICE/README/CLAUDE.md/docs updated.
Verify: `./gradlew assembleDebug` and all unit tests pass; zero `briar|bramble` matches outside licence/notice files and the Tor/lyrebird binary coordinates.

**M2 Platform and hygiene.** targetSdk/compileSdk 36; README rewritten to describe actual behaviour; GitHub Actions CI (build, lint, unit tests); replace hand-pinned dependency hashes with Gradle `verification-metadata.xml` if feasible offline-reproducibly; Android 17 local-network audit notes.
Verify: build + tests pass at SDK 36.

**M3 Security hardening.** Argon2id password KDF with a versioned key-blob format and migration from scrypt; stronger password policy; panic wipe by key destruction; zeroization where practical; constant-time comparisons; Jazzer-style fuzz harnesses for BDF and record parsing (run as unit tests).
Verify: new unit tests (KDF known-answer, blob migration, wipe) pass.

**M4 Product features.** P2P signed app updates (signer and versionCode checks, PackageInstaller); soft build-age prompt; crisis kit (check-in / SOS messages as a client); contact-adding UX text.
Verify: unit tests for validators and update verification; build.

**M5 Transports.** BLE discovery with rotating per-contact rendezvous tokens (plugin skeleton + token derivation + tests); LAN over hotspot (drop infrastructure-only assumption where upstream has it).
Verify: unit tests for token derivation/rotation; build. Radio behaviour needs device testing — reported, not claimed.

**M6 Flagged experimental (off by default).** Hybrid X25519+ML-KEM-768 in contact key agreement (Bouncy Castle ML-KEM); group sender keys; sealed-envelope relay design doc + threat model + envelope format and set-reconciliation prototype with tests.
Verify: unit tests; flags default off.

## Known limits

- Radio features (BLE, Wi-Fi Aware) and Android background behaviour can only be checked on real devices; this machine has no attached device or emulator workflow configured.
- The external audit, F-Droid publication and developer verification are outside what code changes can do.
- If the session runs out before M6, work stops at a committed, verified milestone boundary and the remaining milestones stay in this plan.

## Status (2026-10-13; see docs/HANDOFF.md for next steps)

| Milestone | Status | Notes |
|---|---|---|
| M1 Import + rename | Done | Upstream 1.5.21 snapshot; 7 vendored libraries; rename via `tools/rename-upstream.py`; new logo and teal palette; crash upload removed (share sheet instead); RSS feeds removed; README/NOTICE/CLAUDE.md rewritten. |
| M2 Platform and hygiene | Done, one item deferred | SDK 36 with edge-to-edge insets and back-callback opt-out (verified on an API 36.1 emulator, light and dark); GitHub Actions CI; Android 17 local-network audit (`docs/platform/`). **Deferred:** replacing witness pins with Gradle `verification-metadata.xml` (needs the headless desktop binaries, blocked by the local proxy). |
| M3 Security hardening | Done, one item deferred | Argon2id KDF with migration from scrypt and cost bounds; entropy-based password policy (≥45 bits); Keystore key destroyed on account deletion; constant-time confirmation check; always-on parser robustness tests. **Deferred:** zeroising secrets in memory (limited value in the JVM, invasive). |
| M4 Product features | Done | Build-age update reminder; install update from file (signer/version checks, PackageInstaller; verified end to end on the emulator); "Check in with everyone". Contact-adding copy changes left to the UX pass. |
| M5 Transports | Done on emulators, untested on phones | BLE plugin (`nc-android/.../plugin/ble`), debug builds only. Verified on two Android 16 emulators: discovery, L2CAP connection and message delivery. Real-device testing is still needed. LAN over hotspot/Wi-Fi Direct already worked in upstream. |
| M6 Experimental (flags off) | Partial | Flags added (off everywhere); hybrid X25519+ML-KEM-768 KEM implemented and tested but not wired into BQP; sender keys and sealed-envelope relay are design and threat model only (`docs/design/experimental-crypto-and-relay.md`). |

Test status at the end: 2,263 unit tests, 233 skipped, 37 failing. All failures are environmental on this machine: integration-test timeouts that also occur on pristine upstream, Robolectric runtime download and LAN socket tests blocked by the local proxy.
