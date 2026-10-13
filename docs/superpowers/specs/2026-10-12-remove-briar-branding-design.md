# Remove Briar/Bramble branding — design

Date: 2026-10-12
Status: approved

## Goal

Remove every Briar/Bramble mention from code, resources, and protocol identifiers, while keeping GPLv3 attribution in licence files only. No existing user base needs migration.

## Decisions

| Area | Decision |
|---|---|
| Class prefix (infra layer, `org.chagolchana.chagol`) | `Bramble*` → `Chagol*` |
| Class prefix (app layer, `org.chagolchana.noconnect`) | `Briar*` → `NoConnect*` |
| Styles / colours / layouts | `Briar*` style → `NoConnect*`; `briar_*` colour/layout → `nc_*` |
| User-visible strings (all locales) | "Briar" → "NoConnect" |
| Protocol strings (ClientId, TransportId, crypto/KDF labels, ID labels) | `org.briarproject.bramble.*` → `org.chagolchana.chagol.*`; `org.briarproject.briar.*` → `org.chagolchana.noconnect.*`. **Breaks wire/DB compatibility with NoConnect 0.15 — accepted.** |
| Runtime keys (intent extras, prefs name, manifest action, process name, content URIs) | renamed with the same scheme |
| ProGuard keep rule | `org.briarproject.**` → `org.chagolchana.**` (bug fix: old rule matched nothing) |
| `app_package` resValue | → `org.chagolchana.noconnect.beta` (bug fix: settings intents targeted Briar's package) |
| Patched prebuilt jars `*-briar.jar` | renamed `*-nc.jar` (loaded by `fileTree('*.jar')`) |
| Tor/GeoIP download URLs on briarproject.org | **unchanged** — only hosting location; replacing it is a roadmap item |
| Licensing | add GPLv3 `LICENSE`, `NOTICE` crediting Briar (and Signal, identicons); fill README "Other projects used". These are the only files allowed to mention Briar. |

## Verification

1. `grep -ri 'briar\|bramble'` over sources returns only LICENSE/NOTICE/README attribution and the Tor URLs.
2. Resource-reference check: every `@style/`, `@color/`, `@layout/`, `R.style.`, `R.color.`, `R.layout.` reference resolves to a defined resource.
3. Pure-Java modules (`nc-api`, `nc-core`, `noconnect-api`, `noconnect-core`) compile with `javac --release 8` against Maven Central dependencies, matching the pre-rename baseline.
4. Android modules cannot be compiled on the current toolchain (needs JDK 8 + SDK 23 + build-tools 25.0.3); this is reported as a known gap, not claimed.

## Out of scope

Toolchain modernisation, behaviour changes, new features (covered by the separate architecture deep dive).
