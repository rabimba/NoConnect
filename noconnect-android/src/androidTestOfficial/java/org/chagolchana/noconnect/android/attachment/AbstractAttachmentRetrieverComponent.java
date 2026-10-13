package org.chagolchana.noconnect.android.attachment;

import org.chagolchana.noconnect.android.attachment.media.MediaModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		MediaModule.class
})
interface AbstractAttachmentRetrieverComponent {

	void inject(AttachmentRetrieverIntegrationTest test);

}
