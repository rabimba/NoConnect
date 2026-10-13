# BLE discovery and transport: design

Status: **implemented, debug builds only** (`FeatureFlags.shouldEnableBleTransport()`), as `nc-android/.../plugin/ble/AndroidBlePlugin`. It is verified on two Android 16 emulators: discovery, L2CAP connection, BTP and message delivery all work. It is not yet tested on real phones. Date: 2026-10-13.

## Goal

Contacts who are near each other should find each other and sync without:
- Bluetooth Classic discovery, which recent Android versions restrict
- the user pressing a button
- broadcasting anything that identifies them

## Discovery: rotating rendezvous tokens (as implemented)

- Each device keeps one random 32-byte **rendezvous secret** in its BLE transport properties (`BleConstants.PROP_SECRET`). Transport properties are synced only to contacts, over encrypted BTP, so only contacts know it.
- Every 15 minutes (`EPOCH_MS`), the device advertises **one** token, `MAC(label, secret, epoch)[0..8)`, plus its L2CAP channel number. These go in service data under `BleConstants.SERVICE_UUID`, which with the flags field exactly fills a 31-byte legacy advertisement.
- Scanners recompute each contact's expected token from that contact's synced secret, allowing one epoch of clock difference either side, and connect only to contacts they have seen recently.
- Advertising restarts at each epoch boundary, so the token and (on most devices) the random BLE address rotate together.

**Trade-offs and properties:**
- One token per device scales to any number of contacts.
- Observers can't link tokens across epochs. When a contact is removed, `AndroidBlePlugin` rotates the rendezvous secret immediately and advertises a fresh token; the new secret is synced only to remaining contacts, preventing removed contacts from recognising future tokens.
- An earlier draft derived tokens from per-contact transport tag keys. That would hide you even from removed contacts without re-advertising, but needs one advertisement per contact and access to keys from the plugin layer.
- The new contact's secret arrives only after the first sync over some transport, or via contact exchange. Contact exchange already sends local transport properties, so the secret is included once BLE is enabled on both sides.

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
