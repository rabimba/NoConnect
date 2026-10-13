package org.chagolchana.noconnect.android.conversation.glide;

import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.noconnect.api.attachment.AttachmentReader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
public class NoConnectDataFetcherFactory {

	private final AttachmentReader attachmentReader;
	@DatabaseExecutor
	private final Executor dbExecutor;

	@Inject
	public NoConnectDataFetcherFactory(AttachmentReader attachmentReader,
			@DatabaseExecutor Executor dbExecutor) {
		this.attachmentReader = attachmentReader;
		this.dbExecutor = dbExecutor;
	}

	NoConnectDataFetcher createNoConnectDataFetcher(AttachmentHeader model) {
		return new NoConnectDataFetcher(attachmentReader, dbExecutor, model);
	}

}
