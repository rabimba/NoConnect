# NoConnect architecture review and roadmap

Date: 2026-10-12 · Scope: whole repository at branch `rename/remove-briar-branding` · Goal: a real-world product for disasters, protests and no-connectivity areas.

Method: three read-only code reviews (security core; sync, database and transports; clients, Android app and build), three literature and ecosystem surveys (offline networking, cryptography, product and modernisation), and spot checks of every headline claim against the code. File references are relative to the repo root.

---

## 1. Summary

1. **The app cannot run where it matters.** It targets SDK 22. Android 14 refuses to install apps targeting below 23, and Android 15 below 24. It has no runtime permissions, no notification channel, no foreground-service type, and its background service is `START_NOT_STICKY`. On a current phone it either won't install or will stop syncing in the background.
2. **"Off the grid swarm" isn't what the code does.** NoConnect syncs directly between people who have already added each other as contacts. Forum, blog and group posts flood along the friend graph. There is no discovery of strangers, no multi-hop routing and no mesh. Off-grid transport is Bluetooth Classic only. LAN sync needs an infrastructure Wi-Fi network, and Tor publishes v2 onions, which the Tor network removed in 2021.
3. **The security core is sound in design but dated in parameters.** The 2017 protocols (QR key agreement, time-rotated transport keys, XSalsa20-Poly1305 streams) are reasonable. Weak points:
   - The database key is protected only by PBKDF2, with a calibration bug and a 6-character password floor.
   - There is no post-compromise security and no post-quantum protection.
   - Crash reports are addressed to the upstream project's key and onion.
   - The 2023 ETH Zürich audit fixes made upstream are missing.
4. **The foundation has rotted.**
   - Gradle 4.1 milestone, AGP 2.3.2, jcenter (shut down), Java 1.6 source level, Dagger 2.0.2, support library 23.
   - Tor binaries are downloaded from a third-party host.
   - Zero tests, no CI, and build output committed to git.
5. **The niche is open.** Briar, the upstream project, entered maintenance mode in July 2026. Its own stated gaps are battery use, background reliability, backup, attachments, and hard contact-adding and offline UX. A maintained, modern successor in this space has a clear reason to exist.

**The key decision** (section 4): rebase onto current upstream Briar 1.5.x and re-apply the rename, or modernise the 2017 code in place. We recommend rebasing.

---

## 2. How the system works today

### 2.1 Layers

```
noconnect-android   UI, controllers, NoConnectService, Dagger AndroidComponent
        │
noconnect-core      clients: messaging, forums, blogs, private groups, introductions, sharing, RSS feeds
noconnect-api       client interfaces, ClientIds
        │
nc-android          Android transports: Bluetooth Classic, LAN TCP, Tor
nc-core             crypto, H2 database, sync protocol, transport keys, plugins, lifecycle, events
nc-api              interfaces, value types, constants
(nc-j2se            desktop transports: unused by the app)
```

Each subsystem is an interface in an `*-api` module, an `*Impl` in a `*-core` module, and a Dagger module that binds it. Clients register validators and hooks as a side effect of construction, which is why the `*EagerSingletons` interfaces exist.

### 2.2 Message path

1. `MessagingManagerImpl.addLocalMessage` (noconnect-core/…/messaging/MessagingManagerImpl.java:111) stores the message through `DatabaseComponentImpl.addLocalMessage` (nc-core/…/db/DatabaseComponentImpl.java:221). That creates a status row for every contact the group is visible to.
2. After the commit, events fire synchronously on the database thread (event/EventBusImpl.java:31). `DuplexOutgoingSession` (sync/DuplexOutgoingSession.java:161) queues an offer.
3. The sync protocol is Offer → Request → Message → Ack, in records of at most 48 KiB. Retransmission uses exponential backoff, re-queried every 60 s.
4. `ConnectionManagerImpl` frames records into a stream. The stream starts with a pseudorandom tag derived from per-contact transport keys, and continues as XSalsa20-Poly1305 frames.
5. On the receiving side, `IncomingSession` reads the tag, recognises the contact, stores the message, and `ValidationManagerImpl` runs the client's validator. The client's hook decides whether to re-share the message: private messages return false, forum, blog and group posts return true.

### 2.3 Connections

`Poller` tries each transport against every contact that isn't connected, every 1–10 minutes, with 60 s to 10 min backoff. That costs O(contacts × transports) connection attempts per cycle. It relies on a `ScheduledExecutorService`, which stops while Android dozes. Peers that aren't contacts are dropped at the tag lookup (ConnectionManagerImpl.java:262).

### 2.4 Security protocols

| Flow | Mechanism | Assessment |
|---|---|---|
| Adding a contact | QR codes carry a commitment to an ephemeral Brainpool-P256 key. ECDH over Bluetooth or LAN, then confirmation MACs. | Sound. The curve and library are dated; upstream moved to X25519. |
| Contact exchange | Long-term ECDSA identity signs a nonce derived from the master secret. | Sound. |
| Transport keys | Per-contact, per-transport keys. A hash ratchet rotates them each period (24 h + latency). | Forward secrecy only. Seizing current keys exposes all future traffic, and the previous period is kept about 1–2 days. |
| Streams | Random per-stream frame key under a header key; XSalsa20-Poly1305 frames of at most 1 KiB. | Sound. |
| Database at rest | Random 32-byte key; H2 AES file encryption. The key is wrapped with PBKDF2-SHA256 (about 500 ms) and stored in SharedPreferences. | Weak against offline brute force after seizure. |
| Legacy invitation codes | 6-digit codes; the Bluetooth UUID is derived from the codes. | Broken (see F7). Unreachable from the UI, but still wired. |

### 2.5 What works off-grid

Works off-grid:
- private messages
- forums
- blogs
- private groups and invitations
- introductions
- sharing
- QR contact exchange
- panic button

Needs the internet:
- RSS feeds (fetched over Tor)
- the Tor transport
- crash-report upload
- the build itself (Tor binaries are downloaded)

---

## 3. Findings

Severity: **P0** means fix before anyone relies on the app, **P1** means needed for a credible product, and **P2** is quality and debt.

| # | Sev | Finding | Evidence |
|---|---|---|---|
| F1 | P0 | Won't install on Android 14+, and Play requires target SDK 36 for new apps and updates since 31 Aug 2026. | noconnect-android/build.gradle:76-77 (min 14, target 22) |
| F2 | P0 | No runtime permissions anywhere (camera, location, Bluetooth scan/connect/advertise, notifications). PendingIntents lack FLAG_IMMUTABLE, which crashes on target 31+. No notification channel. No foreground-service type. `startService` from the background is blocked on Android 8+. | No `requestPermissions` calls; NoConnectService.java:77,86,147; NoConnectControllerImpl.java:58 |
| F3 | P0 | Crash reports are encrypted to the upstream project's developer key and addressed to its v2 onion. If the transport worked, NoConnect users' logs, device data and Wi-Fi IP would go to a third party. Reports also pile up on disk. | nc-api/…/reporting/ReportingConstants.java:10,24; NoConnectReportPrimer.java:166-174 |
| F4 | P0 | Tor is unusable: tor 0.2.9.11, v2 onions, binaries downloaded from a third-party host. The build fails if that host changes. | nc-android/build.gradle:35-60; TorPlugin.java:436-460 |
| F5 | P0 | Upstream's 2023 security fixes are missing: a DoS from an invalid-length message, duplication of non-canonical group messages, and the handshake redesign. | briarproject.org/news/2023-three-security-issues-found-and-fixed |
| F6 | P1 | The PBKDF2 calibration computes the median as `a + b/2` instead of `(a+b)/2`, which lowers the iteration count. There is no iteration floor, the count isn't authenticated, and passwords pass with 6 distinct characters. | CryptoComponentImpl.java:667; PasswordStrengthEstimatorImpl.java:18-23 |
| F7 | P1 | The legacy invitation check compares the peer's commitment with a hash of *our own* key, so the flow never verifies the peer. 19-bit codes. Logged Bluetooth UUIDs. | invitation/Connector.java:113-115 |
| F8 | P1 | Background sync dies under Doze and app standby. Polling is blind and battery-hungry. | Poller.java:164; DroidtoothPlugin.java:290-312 |
| F9 | P1 | Off-grid reach is limited to Bluetooth Classic, about 10 m, one link per contact. LAN requires an infrastructure network (`TYPE_WIFI`), so hotspots don't work. There is no BLE, Wi-Fi Direct, Wi-Fi Aware or removable-drive sync. | AndroidLanTcpPlugin.java:67 |
| F10 | P1 | The database can't migrate: `SCHEMA_VERSION == MIN_SCHEMA_VERSION`, so any schema change wipes user data. Messages are never pruned. | JdbcDatabase.java:71-72,287-293 |
| F11 | P1 | Zero tests and no CI, despite declared test dependencies. | No `src/test` directories |
| F12 | P1 | No update path. The expiry lock was removed, so old, vulnerable builds live forever. The banner shows negative days remaining. | NavDrawerControllerImpl.java:106-131 |
| F13 | P2 | Threading: a single database thread behind a global lock; synchronous event bus; unbounded IO pool; non-atomic duplicate-connection race; deprecated `runOnDbThread` used 75 times; AsyncTask and raw Threads in the UI. | DatabaseComponentImpl.java:90; Poller.java:133-136 |
| F14 | P2 | God classes: ConversationActivity (1060 lines), AndroidNotificationManagerImpl (722 lines, five near-identical builders). One ActivityComponent with about 53 inject methods. | |
| F15 | P2 | Secrets are never zeroed. A password change doesn't rotate the database key. A malicious contact can force about 20k key-rotation loops with timestamp 0. Comparisons aren't constant-time. | SecretKey.java; PasswordControllerImpl.java:59-76; TransportKeyManagerImpl.java:171-177 |
| F16 | P2 | Hygiene: 155 tracked build and IDE files; READ_LOGS permission; minify on in debug; only 7 content descriptions; 7 locales. | |

---

## 4. Strategic decision: rebase or modernise in place

| Option | What it means | Pros | Cons |
|---|---|---|---|
| **A. Rebase onto Briar 1.5.x (recommended)** | Start from upstream master (targetSdk 35, AGP 8, Dagger 2.51, X25519/Ed25519, BTP v4, scrypt plus Keystore, tor-android, Mailbox, removable-drive sync, offline app sharing, a full unit-test suite). Re-run this branch's rename script, then port NoConnect's few local changes. | Gets about 7 years of fixes, including F1, F2, F4, F5, F6, F10 and most of F11. Upstream's test suite comes along. The rename is scripted and repeatable. | We inherit upstream's architecture, including H2 and gradle-witness. Upstream is in maintenance mode, so we own every change from then on. It's a protocol break, which we accepted already. |
| B. Modernise in place | Upgrade the 2017 code step by step: toolchain, AndroidX, SDK targets, crypto, tests. | Full control. No large merge. | Re-implements, slowly and with no tests, work upstream has already done and audited. Realistically 2–3× the effort of A before we reach parity. |
| C. New core | Write a fresh Kotlin Multiplatform core with a new protocol. | Clean slate for a mesh-first design. | Highest risk. Discards audited protocols. Months before feature parity. |

**Recommendation: A.** Run it as a time-boxed spike first (about 2 weeks): rebase, rename, build, and run upstream's tests under the NoConnect namespace. Then decide. Everything in sections 5–6 is written so it applies under A or B. Items that A gives us free are marked **(A: free)**.

---

## 5. Architecture roadmap

### Phase 0 — Stop the bleeding (1–2 weeks, any option)

- Disable crash reporting, or point it at a NoConnect-owned key and v3 onion with explicit opt-in. Drop logcat and the Wi-Fi IP. (F3, S)
- Fix the median bug. Add an iteration floor and an upper bound on decrypt. Raise the password policy. (F6, S)
- Delete the legacy invitation path: `core/invitation`, `AddContactActivity`, the code-derived UUIDs. (F7, S)
- Make the README describe the app as friend-to-friend sync, not a swarm. (S)
- Untrack `build/`, `.gradle/`, `.idea/` and `*.iml`. Remove READ_LOGS. (F16, S)
- Add a GitHub Actions skeleton running the pure-Java compile and unit tests. The javac harness from this review is a starting point. (F11, S)

### Phase 1 — A buildable, installable app (6–10 weeks)

- **Toolchain** (A: free): current Gradle and AGP, JDK 17, mavenCentral/google, `implementation` and `annotationProcessor`, current Dagger. Gradle `verification-metadata.xml` replaces gradle-witness. Java 8+ source level with desugaring. (M)
- **SDK and platform** (A: mostly free): targetSdk 36.
  - Runtime-permission flow, with `BLUETOOTH_SCAN` declared `neverForLocation`.
  - Android 12+ Bluetooth permissions, Android 13 notifications and `NEARBY_WIFI_DEVICES`.
  - Notification channels, FLAG_IMMUTABLE.
  - A `connectedDevice` foreground service that restarts automatically if killed.
  - A battery-optimisation exemption prompt.
  - An audit against Android 17's `ACCESS_LOCAL_NETWORK` for LAN.
  - Native libraries built for 16 KB pages. (L)
- **Tor**: tor-android with v3 onions, or a build flavour without Tor. Decide whether RSS feeds belong in an off-grid product at all. (F4, M)
- **Tests** (A: free): port upstream's unit tests for crypto, sync, the database and client state machines. Add known-answer tests before touching crypto. (F11, M)
- **Database**: incremental migrations using upstream's `Migration` chain, plus retention and pruning with per-group quotas. Keep H2 for now. Consider SQLCipher only if a Kotlin Multiplatform core becomes a goal. (F10, M)
- **Reliability**: an asynchronous event executor, debounced offer generation, atomic de-duplication of connections, alarm-driven polling. (F8, F13, M)

### Phase 2 — Security hardening (4–6 weeks)

- **Database key**: Argon2id (≥64 MiB, t=3, auto-tuned) or scrypt, with a versioned blob, wrapped by a non-exportable Android Keystore or StrongBox HMAC key. Rotate the database key when the password changes. (L–M)
- **Panic and duress**: wipe by destroying keys (delete the Keystore key, overwrite the blob, then delete files), plus an optional duress PIN. Market this as "fast wipe", not deniability. (S–M)
- **Disappearing messages** with per-message keys deleted at expiry (crypto-shredding). (M)
- Zero secrets, use char[] passwords, constant-time comparisons, and clamp contact timestamps. (F15, S)
- **Hybrid post-quantum contact setup**: X25519 + ML-KEM-768 in the QR and remote-contact handshakes. That costs about 2.3 KB once per contact; transport keys stay symmetric. (M)
- **Assurance**:
  - Jazzer fuzzing of every decoder (BDF, records, frames) in CI.
  - Tamarin models of the contact handshakes. No public model exists, and upstream's 2023 handshake flaw shows the need.
  - An external audit before any "safe for protests" claim. (M–L)

---

## 6. Feature roadmap

Ranked by impact for people in a crisis.

| # | Feature | Why | Effort | Depends on |
|---|---|---|---|---|
| 1 | **Offline app sharing and peer-to-peer updates** | In real shutdowns the biggest win has been getting the app onto phones and having contacts set up beforehand. Share the APK over a local hotspot with a download page. Fetch newer signed APKs from contacts, check the signer and versionCode, and install via PackageInstaller. Replace hard expiry with a "this build is N months old" prompt. | M | Phase 1 |
| 2 | **Distribution** | Register as a verified developer: Google's developer verification adds a one-day wait for unregistered apps, rolling out globally in 2027. Publish on F-Droid with reproducible builds. | M | Phase 1 |
| 3 | **Modern off-grid transports** | BLE discovery using rotating per-contact rendezvous tokens, PRF(k_contact, epoch), rotated with the random MAC address, so only nearby contacts get dialled. L2CAP CoC for small payloads. Wi-Fi Aware where the hardware supports it, otherwise Wi-Fi Direct or a local-only hotspot, for bulk transfer. LAN over hotspots plus mDNS. Removable-drive (USB/SD) sync (A: free). | L | Phase 1 |
| 4 | **Contact-adding without friction** | One QR scan with verification built in. Introductions shown as the normal way to grow a network. Plain-language security prompts inside the flow, as the usability studies found. | M | |
| 5 | **Crisis kit** | "I'm safe" check-ins, SOS broadcast to contacts with optional location, CAP 1.2 alert import and export for NGO interoperability, panic wipe (Phase 2). | M | |
| 6 | **Group end-to-end keys** | Today group posts are protected only on each link and stored as plaintext by every member, with no cryptographic removal. Add sender keys sent over the pairwise links and rotated on membership change; later move to DCGKA (Weidner et al., CCS 2021), which only needs causal delivery and so fits this sync model better than MLS. | M → L | Phase 2 |
| 7 | **Opt-in relay of sealed envelopes** (the real "swarm") | A new layer between transports and clients. Envelopes are encrypted end to end to the recipient and addressed by a recipient tag only they can recognise. They are padded to fixed sizes, with a TTL and hop limit, and copy-limited by Spray-and-Wait. Range-based set reconciliation (Negentropy/RBSR) syncs envelope sets during short encounters. A bounded relay cache with trust tiers: contacts, then friends of friends, then strangers. Requires a written threat model and abuse budget first: Bridgefy was broken twice for skipping this. | L | 3, 6, threat model, simulation in The ONE / ns-3 |
| 8 | **Accessibility, localisation, backup** | Icon-first UI, right-to-left layouts, TalkBack, large fonts, field tests with NGOs. Encrypted account backup is upstream's top unsolved request. | M | |
| 9 | **Long-range bridge** (later) | Meshtastic/LoRa companion radio over BLE serial for short text across kilometres. Never trust its default channel key. | L | 3 |

Deliberately not recommended:
- **Google Nearby Connections as the main transport.** It depends on closed-source Play Services and is unavailable on de-Googled phones. At most, an optional plugin.
- **Plain RFC 9420 MLS for groups.** It needs a central orderer this system doesn't have.
- **Proof-of-work anti-spam.** Too costly in battery.
- **Hidden-volume deniability.** Fragile on flash storage.

---

## 7. Suggested sequence

```
Weeks  1-2   Phase 0 hygiene + rebase spike (decision gate: A or B)
Weeks  3-12  Phase 1 foundation  ─┐
Weeks  8-14  Phase 2 hardening    ├─ features 1, 2, 4 in parallel once Phase 1 builds
Weeks 12-24  Feature 3 transports ┘
Weeks 16-28  Features 5, 6 (sender keys), 8
Weeks 24+    Threat model + simulation → feature 7 relay; external audit
```

## 8. Open decisions for the team

1. Rebase onto upstream (A) or modernise in place (B)? This is decided by the 2-week spike.
2. Keep Tor and RSS feeds, or ship off-grid only, with internet transports as a separate flavour?
3. Crash reporting: remove it, keep it local-only, or run an opt-in endpoint we own?
4. Is an open public local channel, like Bitchat's #mesh, in scope? It drives adoption but needs strict labelling and abuse controls.
5. Funding and ownership of an external audit before any public crisis deployment.

## 9. Sources

Upstream specs and project:
- BTP, BQP and BHP specs: https://code.briarproject.org/briar/briar-spec
- Maintenance mode: https://briarproject.org/news/2026-maintenance-mode/
- 2023 security fixes: https://briarproject.org/news/2023-three-security-issues-found-and-fixed/
- 1.4 offline sharing and removable drives: https://briarproject.org/news/2021-briar-1.4-released/
- Mailbox: https://briarproject.org/news/2023-briar-mailbox-released/

Android platform:
- Android 14 minimum target SDK: https://developer.android.com/about/versions/14/behavior-changes-all
- Play target-SDK requirement: https://developer.android.com/google/play/requirements/target-sdk
- Bluetooth permissions: https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- Foreground-service types: https://developer.android.com/develop/background-work/services/fg-service-types
- Local-network permission: https://developer.android.com/privacy-and-security/local-network-permission
- Wi-Fi Aware: https://developer.android.com/develop/connectivity/wifi/wifi-aware
- Developer verification: https://developer.android.com/developer-verification

Mesh and delay-tolerant networking:
- Bundle Protocol v7: RFC 9171
- PRoPHET: RFC 6693
- Spray-and-Wait: https://doi.org/10.1145/1080139.1080143
- Range-based set reconciliation: https://arxiv.org/abs/2212.13567
- Negentropy: https://github.com/hoytech/negentropy
- Rangzen: https://arxiv.org/abs/1612.03371
- Berty protocol: https://berty.tech/docs/protocol

Bridgefy analyses:
- Albrecht, Blasco, Jensen, Mareková, CT-RSA 2021: https://eprint.iacr.org/2021/214
- Albrecht, Eikenberg, Paterson, USENIX Security 2022: https://www.usenix.org/conference/usenixsecurity22/presentation/albrecht

Cryptography:
- FIPS 203 (ML-KEM): https://csrc.nist.gov/pubs/fips/203/final
- X-Wing: https://datatracker.ietf.org/doc/draft-connolly-cfrg-xwing-kem/
- PQXDH: https://signal.org/docs/specifications/pqxdh/
- SPQR: https://signal.org/blog/spqr/
- iMessage PQ3: https://security.apple.com/blog/imessage-pq3/
- MLS: RFC 9420
- DMLS draft: https://datatracker.ietf.org/doc/draft-kohbrok-mls-dmls/
- DCGKA: https://eprint.iacr.org/2020/1281
- Argon2: RFC 9106
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- BLE tracking: https://petsymposium.org/popets/2019/popets-2019-0036.php
- Tamarin: https://tamarin-prover.com/
- Jazzer: https://github.com/CodeIntelligenceTesting/jazzer

Usability:
- Hong Kong protest study, USENIX Security 2021: https://www.usenix.org/conference/usenixsecurity21/presentation/albrecht
- Key-verification studies, SOUPS 2017: https://www.usenix.org/conference/soups2017/technical-sessions/presentation/vaziripour
- Key-verification studies, SOUPS 2018: https://www.usenix.org/conference/soups2018/presentation/vaziripour
- CAP 1.2: https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2-os.html

Testing:
- Emulator networking: https://developer.android.com/studio/run/emulator-networking
- The ONE simulator: https://akeranen.github.io/the-one/
- Shadow: https://shadow.github.io/

Signal URLs were cited from prior knowledge because the proxy blocked them; all other URLs were fetched during research.
