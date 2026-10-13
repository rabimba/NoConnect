package org.chagolchana.noconnect.android.attachment;

import android.net.Uri;

import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

@NotNullByDefault
public interface AttachmentCreator {

	@UiThread
	LiveData<AttachmentResult> storeAttachments(LiveData<GroupId> groupId,
			Collection<Uri> newUris);

	/**
	 * This should be only called after configuration changes.
	 * In this case we should not create new attachments.
	 * They are already being created and returned by the existing LiveData.
	 */
	@UiThread
	LiveData<AttachmentResult> getLiveAttachments();

	@UiThread
	List<AttachmentHeader> getAttachmentHeadersForSending();

	/**
	 * Marks the attachments as sent and adds the items to the cache for display
	 *
	 * @param id The MessageId of the sent message.
	 */
	@UiThread
	void onAttachmentsSent(MessageId id);

	/**
	 * Needs to be called when created attachments will not be sent anymore.
	 */
	@UiThread
	void cancel();

	@UiThread
	void deleteUnsentAttachments();

	@IoExecutor
	void onAttachmentHeaderReceived(Uri uri, AttachmentHeader h,
			boolean needsSize);

	@IoExecutor
	void onAttachmentError(Uri uri, Throwable t);

	@IoExecutor
	void onAttachmentCreationFinished();
}