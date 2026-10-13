package org.chagolchana.noconnect.android.blog;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.identity.LocalAuthor;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.activity.NoConnectActivity;
import org.chagolchana.noconnect.android.view.TextInputView;
import org.chagolchana.noconnect.android.view.TextSendController;
import org.chagolchana.noconnect.android.view.TextSendController.SendListener;
import org.chagolchana.noconnect.api.android.AndroidNotificationManager;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.BlogPost;
import org.chagolchana.noconnect.api.blog.BlogPostFactory;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.text.util.Linkify.WEB_URLS;
import static android.text.util.Linkify.addLinks;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
import static androidx.core.text.HtmlCompat.toHtml;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;
import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.StringUtils.isNullOrEmpty;
import static org.chagolchana.chagol.util.StringUtils.toUtf8;
import static org.chagolchana.noconnect.android.view.TextSendController.SendState;
import static org.chagolchana.noconnect.android.view.TextSendController.SendState.SENT;
import static org.chagolchana.noconnect.api.blog.BlogConstants.MAX_BLOG_POST_TEXT_LENGTH;
import static org.chagolchana.noconnect.util.HtmlUtils.cleanArticle;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class WriteBlogPostActivity extends NoConnectActivity
		implements SendListener {

	private static final Logger LOG =
			Logger.getLogger(WriteBlogPostActivity.class.getName());

	@Inject
	AndroidNotificationManager notificationManager;

	private TextInputView input;
	private ProgressBar progressBar;

	// Fields that are accessed from background threads must be volatile
	private volatile GroupId groupId;
	@Inject
	volatile IdentityManager identityManager;
	@Inject
	volatile BlogPostFactory blogPostFactory;
	@Inject
	volatile BlogManager blogManager;

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		Intent i = getIntent();
		byte[] b = i.getByteArrayExtra(GROUP_ID);
		if (b == null) throw new IllegalStateException("No Group in intent.");
		groupId = new GroupId(b);

		setContentView(R.layout.activity_write_blog_post);

		input = findViewById(R.id.textInput);
		TextSendController sendController =
				new TextSendController(input, this, false);
		input.setSendController(sendController);
		input.setMaxTextLength(MAX_BLOG_POST_TEXT_LENGTH);
		input.setReady(true);

		progressBar = findViewById(R.id.progressBar);
	}

	@Override
	public void onStart() {
		super.onStart();
		notificationManager.blockNotification(groupId);
	}

	@Override
	public void onStop() {
		super.onStop();
		notificationManager.unblockNotification(groupId);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public LiveData<SendState> onSendClick(@Nullable String text,
			List<AttachmentHeader> headers, long expectedAutoDeleteTimer) {
		if (isNullOrEmpty(text)) throw new AssertionError();

		SpannableStringBuilder ssb = SpannableStringBuilder.valueOf(text);
		addLinks(ssb, WEB_URLS);
		String html = cleanArticle(toHtml(ssb,
				TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));

		int textLength = toUtf8(html).length;
		if (textLength > MAX_BLOG_POST_TEXT_LENGTH) {
			Snackbar.make(input, R.string.text_too_long, LENGTH_SHORT).show();
			return new MutableLiveData<>(null);
		}

		// hide publish button, show progress bar
		input.hideSoftKeyboard();
		input.setVisibility(GONE);
		progressBar.setVisibility(VISIBLE);

		storePost(html);
		return new MutableLiveData<>(SENT);
	}

	private void storePost(String text) {
		runOnDbThread(() -> {
			long timestamp = System.currentTimeMillis();
			try {
				LocalAuthor author = identityManager.getLocalAuthor();
				BlogPost p = blogPostFactory
						.createBlogPost(groupId, timestamp, null, author, text);
				blogManager.addLocalPost(p);
				postPublished();
			} catch (DbException | GeneralSecurityException
					| FormatException e) {
				logException(LOG, WARNING, e);
				postFailedToPublish();
			}
		});
	}

	private void postPublished() {
		runOnUiThreadUnlessDestroyed(() -> {
			setResult(RESULT_OK);
			supportFinishAfterTransition();
		});
	}

	private void postFailedToPublish() {
		runOnUiThreadUnlessDestroyed(() -> {
			// hide progress bar, show publish button
			progressBar.setVisibility(GONE);
			input.setVisibility(VISIBLE);
			// TODO show error
		});
	}
}
