package org.chagolchana.noconnect.android.conversation.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.InputStream;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;
import static org.chagolchana.noconnect.android.TestingConstants.IS_DEBUG_BUILD;

@GlideModule
@NotNullByDefault
public final class NoConnectGlideModule extends AppGlideModule {

	@Override
	public void registerComponents(Context context, Glide glide,
			Registry registry) {
		NoConnectApplication app =
				(NoConnectApplication) context.getApplicationContext();
		NoConnectModelLoaderFactory factory = new NoConnectModelLoaderFactory(app);
		registry.prepend(AttachmentHeader.class, InputStream.class, factory);
	}

	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		builder.setLogLevel(IS_DEBUG_BUILD ? DEBUG : WARN);
	}

	@Override
	public boolean isManifestParsingEnabled() {
		return false;
	}

}