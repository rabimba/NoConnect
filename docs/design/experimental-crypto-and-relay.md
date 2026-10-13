# Experimental cryptography and relay: design and threat model

Date: 2026-10-13. All three features are behind `FeatureFlags` (`shouldEnableHybridPqKeyAgreement`, `shouldEnableGroupSenderKeys`, `shouldEnableSealedEnvelopeRelay`) and are **disabled in every build**. Don't enable any of them until the design has been reviewed, modelled (Tamarin/ProVerif where noted) and audited.

## 1. Hybrid post-quantum key agreement

**Implemented:** `nc-core/.../crypto/HybridKem` combines X25519 and ML-KEM-768 using the X-Wing combiner, `SHA3-256(ss_M ‖ ss_X ‖ ct_X ‖ pk_X ‖ label)`. It has unit tests for round trips, tampering with either half, wrong keys and wrong lengths. No protocol uses it yet.

**Integration plan:**
- **Contact exchange over QR (BQP).** Each QR payload gains a hybrid public key commitment. After the existing X25519 exchange, each side encapsulates to the other's hybrid key over the established link. The master key then becomes `KDF(ss_X25519_BQP ‖ ss_hybrid_A→B ‖ ss_hybrid_B→A, transcript)`.
- **Size and protocol version.** This adds about 2.4 KB each way, once per contact, and needs a new BQP protocol version. QR codes stay small because only commitments go in them.
- **Remote contacts (handshake protocol).** The same pattern applies, in a new handshake version.
- **After setup.** Transport keys stay symmetric (BTP rotation), so no per-message cost.
- **Signatures.** Long-term identity signatures remain Ed25519. Moving authorship to ML-DSA is less urgent, because signatures protect authenticity rather than confidentiality, and about 40× larger per message.

**Before enabling:**
- Write a Tamarin model of the modified BQP covering secrecy, key confirmation and agreement, including a downgrade attack where an attacker strips the hybrid part.
- Version negotiation must fail closed once both sides support the hybrid scheme.
- Add known-answer tests against an independent ML-KEM implementation.

## 2. Group sender keys

**Problem:** forum and private-group posts are encrypted only per link. Every member stores plaintext, and removing a member doesn't revoke anything cryptographically.

**Design (Signal-style sender keys over pairwise links):**
- Each member keeps a sender chain per group: `ck_{i+1} = KDF(ck_i, "chain")`, `mk_i = KDF(ck_i, "msg")`.
- The member distributes the chain key to each other member as a private message over the existing pairwise BTP links.
- Posts are encrypted with `mk_i`, carry the sender's chain index, and remain signed by the author as today.
- When membership changes, every remaining member rotates their chain and redistributes it. Removed members can't read posts after the rotation. They can still read posts they already held.
- Fits the existing client and validator pattern as a new client version. The validator checks the signature and the decryptability of what is shared.

**Limits:**
- No post-compromise security until the next rotation.
- Fan-out is O(n) per rotation.
- Concurrent membership changes need a deterministic merge rule.

**Later:** DCGKA (Weidner et al., CCS 2021) gives post-compromise security using only causal delivery, which this sync model provides. MLS (RFC 9420) needs a central orderer and doesn't fit.

## 3. Sealed-envelope relay ("swarm")

**Goal:** deliver messages through phones that aren't the recipient's contacts, while revealing as little as possible.

**Envelope:**
```
version (1) ‖ recipient tag (16) ‖ expiry (4) ‖ hop limit (1) ‖ copies (1) ‖ ciphertext (padded to 1, 4 or 16 KiB)
```
- **Recipient tag:** derived from a per-contact rotating secret (like the BLE rendezvous tokens), so only the intended recipient can recognise it and relays learn nothing.
- **Ciphertext:** BTP-encrypted to the recipient. The sender's identity is visible only inside it.

**Routing:**
- Spray-and-Wait with an initial copy count of 8, and the hop limit decremented at each relay.
- Range-based set reconciliation (Negentropy-style) of envelope IDs during short encounters.
- A bounded relay cache (default 64 MB) with priority tiers: contacts, then friends of friends (via introductions), then strangers. Each tier has a quota, and the oldest envelopes are evicted first within a tier.

**Threat model:**

| Threat | Mitigation |
|---|---|
| Relay reads content | End-to-end encryption to the recipient; relays hold only ciphertext |
| Relay or observer links sender and recipient | Rotating recipient tags; no sender identity outside the ciphertext; fixed padding sizes; random relay delay |
| Tracking a device through its advertisements | Rotating tokens tied to the random BLE address (see ble-discovery.md); no stable identifiers |
| Flooding or DoS (Bridgefy's "one message kills the network") | Strict canonical decoding with length checks before parsing (robustness tests exist for the current parsers); per-encounter byte and time budgets; per-origin token buckets; hop and copy limits; bounded cache |
| Sybil relays absorbing or dropping traffic | Trust tiers favour contacts and introduced peers; Spray-and-Wait copies make a single dropper insufficient |
| Replaying old envelopes | Expiry field plus a seen-ID set kept until expiry |
| Storage exhaustion on low-end phones | Hard cache cap; envelopes from strangers evicted first |

**Before enabling:**
- Simulate density, delivery rate, latency and battery cost in The ONE or ns-3, using realistic mobility traces.
- Write a Tamarin model of the recipient-tag and envelope scheme.
- Fuzz the envelope decoder with the robustness-test pattern.
- Commission an external audit.
- Ship it opt-in, with a clear explanation that relaying uses storage and battery.
