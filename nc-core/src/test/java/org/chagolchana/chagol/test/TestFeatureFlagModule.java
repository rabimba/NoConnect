package org.chagolchana.chagol.test;

import org.chagolchana.chagol.api.FeatureFlags;

import dagger.Module;
import dagger.Provides;

@Module
public class TestFeatureFlagModule {
	@Provides
	FeatureFlags provideFeatureFlags() {
		return new FeatureFlags() {
			@Override
			public boolean shouldEnableImageAttachments() {
				return true;
			}

			@Override
			public boolean shouldEnableProfilePictures() {
				return true;
			}

			@Override
			public boolean shouldEnableDisappearingMessages() {
				return true;
			}

			@Override
			public boolean shouldEnablePrivateGroupsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableForumsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableBlogsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableHybridPqKeyAgreement() {
				return false;
			}

			@Override
			public boolean shouldEnableGroupSenderKeys() {
				return false;
			}

			@Override
			public boolean shouldEnableSealedEnvelopeRelay() {
				return false;
			}
		};
	}
}
