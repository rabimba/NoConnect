package org.chagolchana.noconnect.android.conversation.glide;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.InputStream;

@NotNullByDefault
class NoConnectModelLoaderFactory
		implements ModelLoaderFactory<AttachmentHeader, InputStream> {

	private final NoConnectApplication app;

	NoConnectModelLoaderFactory(NoConnectApplication app) {
		this.app = app;
	}

	@Override
	public ModelLoader<AttachmentHeader, InputStream> build(
			MultiModelLoaderFactory multiFactory) {
		return new NoConnectModelLoader(app);
	}

	@Override
	public void teardown() {
		// noop
	}

}
