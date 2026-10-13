package org.chagolchana.noconnect.android.conversation.glide;


import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.io.InputStream;

import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public final class NoConnectModelLoader
		implements ModelLoader<AttachmentHeader, InputStream> {

	@Inject
	NoConnectDataFetcherFactory dataFetcherFactory;

	NoConnectModelLoader(NoConnectApplication app) {
		app.getApplicationComponent().inject(this);
	}

	@Override
	public LoadData<InputStream> buildLoadData(AttachmentHeader model,
			int width, int height, Options options) {
		ObjectKey key = new ObjectKey(model.getMessageId());
		NoConnectDataFetcher dataFetcher =
				dataFetcherFactory.createNoConnectDataFetcher(model);
		return new LoadData<>(key, dataFetcher);
	}

	@Override
	public boolean handles(AttachmentHeader model) {
		return true;
	}
}
