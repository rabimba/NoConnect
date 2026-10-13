# Android 17 local-network permission: impact audit

Date: 2026-10-13 · Current target: SDK 36 · Source: https://developer.android.com/privacy-and-security/local-network-permission

## What changes

Apps that target SDK 37 lose default access to the local network. To open sockets to LAN addresses (including hotspot and Wi-Fi Direct peers), they must hold the runtime permission `ACCESS_LOCAL_NETWORK`, or use system pickers. Apps targeting 36 or lower keep today's behaviour, so nothing breaks until we raise the target.

## Affected code

| Component | What it does on the local network | Impact at target 37 |
|---|---|---|
| `nc-android/.../plugin/tcp/AndroidLanTcpPlugin` | Listens on and connects to LAN addresses on `wlan*`, `ap*` (hotspot) and `p2p*` (Wi-Fi Direct) interfaces | Wi-Fi sync stops without the permission |
| `noconnect-android/.../hotspot/WebServer`, `WebServerManager` | Serves the APK over HTTP to phones joined to our hotspot (offline app sharing) | Serving to hotspot clients needs the permission |
| Key agreement over LAN (QR flow, `nc-core/.../keyagreement` with the LAN plugin's key-agreement listener) | Exchanges keys with the scanned peer over the LAN | Adding contacts over Wi-Fi fails without it |
| Tor (`nc-lib-onionwrapper-*`) | Connects to the local Tor control/SOCKS ports on loopback | Loopback is not "local network"; no change expected. Verify when targeting 37 |
| Bluetooth, removable drives | Not IP-based | No change |

## Plan for when we target 37

1. Declare `ACCESS_LOCAL_NETWORK` in the manifest.
2. Request it where the app already requests related permissions: `navdrawer/TransportsActivity` and `settings/ConnectionsFragment` (enabling Wi-Fi), `contact/add/nearby/AddNearbyContactPermissionManager` (adding contacts nearby), and `hotspot/ConditionManager29`/`ConditionManager33` (before starting the hotspot web server).
3. Treat denial like Wi-Fi being off: the LAN plugin reports `INACTIVE` with a reason, and the transports screen explains how to grant it.
4. Add an emulator test that runs LAN key agreement between two emulators with the permission granted and denied.

Until then, keep the target at 36.
