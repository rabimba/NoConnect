package org.chagolchana.noconnect.android.forum;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.chagol.api.nullsafety.ParametersNotNullByDefault;
import org.chagolchana.chagol.util.StringUtils;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.activity.BriarActivity;
import org.chagolchana.noconnect.api.forum.Forum;
import org.chagolchana.noconnect.api.forum.ForumManager;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.chagolchana.noconnect.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateForumActivity extends BriarActivity {

	private static final Logger LOG =
			Logger.getLogger(CreateForumActivity.class.getName());

	private EditText nameEntry;
	private Button createForumButton;
	private ProgressBar progress;
	private TextView feedback;

	// Fields that are accessed from background threads must be volatile
	@Inject
	protected volatile ForumManager forumManager;

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_create_forum);

		nameEntry = (EditText) findViewById(R.id.createForumNameEntry);
		nameEntry.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int lengthBefore, int lengthAfter) {
				enableOrDisableCreateButton();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		nameEntry.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent e) {
				createForum();
				return true;
			}
		});

		feedback = (TextView) findViewById(R.id.createForumFeedback);

		createForumButton = (Button) findViewById(R.id.createForumButton);
		createForumButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createForum();
			}
		});

		progress = (ProgressBar) findViewById(R.id.createForumProgressBar);
	}

	@Override
	public void onStart() {
		super.onStart();
		showSoftKeyboard(nameEntry);
	}

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	private void enableOrDisableCreateButton() {
		if (createForumButton == null) return; // Not created yet
		createForumButton.setEnabled(validateName());
	}

	private boolean validateName() {
		String name = nameEntry.getText().toString();
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_FORUM_NAME_LENGTH) {
			feedback.setText(R.string.name_too_long);
			return false;
		}
		feedback.setText("");
		return length > 0;
	}

	private void createForum() {
		if (!validateName()) return;
		hideSoftKeyboard(nameEntry);
		createForumButton.setVisibility(GONE);
		progress.setVisibility(VISIBLE);
		storeForum(nameEntry.getText().toString());
	}

	private void storeForum(final String name) {
		runOnDbThread(new Runnable() {
			@Override
			public void run() {
				try {
					long now = System.currentTimeMillis();
					Forum f = forumManager.addForum(name);
					long duration = System.currentTimeMillis() - now;
					if (LOG.isLoggable(INFO))
						LOG.info("Storing forum took " + duration + " ms");
					displayForum(f);
				} catch (DbException e) {
					if (LOG.isLoggable(WARNING))
						LOG.log(WARNING, e.toString(), e);
					finishOnUiThread();
				}
			}
		});
	}

	private void displayForum(final Forum f) {
		runOnUiThreadUnlessDestroyed(new Runnable() {
			@Override
			public void run() {
				Intent i = new Intent(CreateForumActivity.this,
						ForumActivity.class);
				i.putExtra(GROUP_ID, f.getId().getBytes());
				i.putExtra(GROUP_NAME, f.getName());
				startActivity(i);
				Toast.makeText(CreateForumActivity.this,
						R.string.forum_created_toast, LENGTH_LONG).show();
				supportFinishAfterTransition();
			}
		});
	}
}
