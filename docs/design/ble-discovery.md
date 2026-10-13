# BLE discovery and transport: design

Status: token derivation implemented (`nc-core/.../plugin/ble/BleRendezvousTokens`, tested). The plugin itself is not implemented yet. Date: 2026-10-13.

## Goal

Contacts who are near each other should find each other and sync without:
- Bluetooth Classic discovery, which recent Android versions restrict
- the user pressing a button
- broadcasting anything that identifies them

## Discovery: rotating rendezvous tokens

- Every 15 minutes (`EPOCH_MS`), each device advertises an 8-byte token per contact under a fixed NoConnect service UUID. 8 bytes plus a 128-bit UUID fit in a legacy 31-byte advertisement.
- `token = MAC(label, key, epoch)[0..8)`, where `key` is the sender's **outgoing tag key for the BLE transport in the current rotation period**.
- The receiver matches observed tokens against its **incoming** tag keys for each contact (`BleRendezvousTokens.match`). It allows one epoch of clock difference either side.

Why the transport keys:
- They already exist per contact and per transport. The key manager derives them when a plugin registers a new `TransportId`, so adding the BLE transport needs no new key agreement.
- They are directional, so Alice's advertisements match Bob's incoming keys and nothing else.
- They rotate with forward secrecy, so a seized device doesn't reveal past tokens.

Unlinkability depends on rotating the token **together with the random BLE address**. Android rotates the address roughly every 15 minutes, but not under app control. The plugin should restart advertising, which gets a new address, at each epoch boundary, so an address never spans two tokens.

Advertising one token per contact doesn't scale beyond a handful of contacts. For larger contact lists, use a Bloom filter of the current tokens in an extended advertisement (Android 8+, where the hardware supports it), or advertise a single per-device token and have contacts query it after connecting. Pick one after measuring on real devices.

## Transport

- Use an L2CAP connection-oriented channel (`listenUsingInsecureL2capChannel`, API 29+) for the stream. BTP authenticates and encrypts it, so the link doesn't need Bluetooth pairing.
- The listening PSM changes each time; put it in the advertisement's service data next to the token.
- On API 21–28, fall back to the existing Bluetooth Classic plugin.
- Hand bulk transfers to Wi-Fi (LAN, hotspot or Wi-Fi Direct) when both sides support it.

## Platform requirements

- Permissions: `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN` with `android:usesPermissionFlags="neverForLocation"`, and `BLUETOOTH_CONNECT` (API 31+).
- Background operation: a `connectedDevice` foreground service (the app already runs one).
- Scanning: use filtered scans on the service UUID; unfiltered background scans get throttled. Cap the scan duty cycle when the battery is low.

## Testing

- **Unit:** token derivation and matching (done).
- **Integration:** two emulators (API 31+ emulators support Bluetooth between instances), checking that contacts discover and sync, and that a third device with no shared keys matches nothing.
- **Field:** real phones from several manufacturers, checking discovery latency, battery use per hour, and behaviour under Doze and manufacturer battery killers.
