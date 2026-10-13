package org.chagolchana.chagol.api;

/**
 * Interface for specifying which features are enabled in a build.
 */
public interface FeatureFlags {

	boolean shouldEnableImageAttachments();

	boolean shouldEnableProfilePictures();

	boolean shouldEnableDisappearingMessages();

	boolean shouldEnablePrivateGroupsInCore();

	boolean shouldEnableForumsInCore();

	boolean shouldEnableBlogsInCore();

	/*
	 * Experimental features. They change the wire protocol or add new
	 * cryptography, so they must stay disabled in every build until they
	 * have been reviewed and independently audited. See
	 * docs/design/experimental-crypto-and-relay.md.
	 */

	/**
	 * Hybrid X25519 + ML-KEM-768 key agreement when adding contacts.
	 */
	boolean shouldEnableHybridPqKeyAgreement();

	/**
	 * End-to-end encryption of forum and private group posts with sender
	 * keys.
	 */
	boolean shouldEnableGroupSenderKeys();

	/**
	 * Store-and-forward relay of sealed envelopes for people who aren't
	 * contacts.
	 */
	boolean shouldEnableSealedEnvelopeRelay();
}
