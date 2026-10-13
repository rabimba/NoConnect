package org.chagolchana.noconnect.android.reporting;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.logging.BriefLogFormatter;
import org.chagolchana.noconnect.android.logging.CachingLogHandler;
import org.chagolchana.noconnect.android.logging.LogDecrypter;
import org.chagolchana.noconnect.android.reporting.ReportData.MultiReportInfo;
import org.chagolchana.noconnect.android.reporting.ReportData.ReportItem;
import org.chagolchana.noconnect.android.viewmodel.LiveEvent;
import org.chagolchana.noconnect.android.viewmodel.MutableLiveEvent;
import org.chagolchana.noconnect.api.android.MemoryStats;
import org.chagolchana.noconnect.api.android.NetworkUsageMetrics;
import org.chagolchana.nullsafety.NotNullByDefault;
import org.json.JSONException;

import java.util.logging.Formatter;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.StringUtils.isNullOrEmpty;
import static org.chagolchana.noconnect.android.logging.BriefLogFormatter.formatLog;

@NotNullByDefault
class ReportViewModel extends AndroidViewModel {

	private static final Logger LOG =
			getLogger(ReportViewModel.class.getName());

	private final CachingLogHandler logHandler;
	private final LogDecrypter logDecrypter;
	private final NoConnectReportCollector collector;

	private final MutableLiveEvent<Boolean> showReport =
			new MutableLiveEvent<>();
	private final MutableLiveData<Boolean> showReportData =
			new MutableLiveData<>();
	private final MutableLiveData<ReportData> reportData =
			new MutableLiveData<>();
	private final MutableLiveEvent<Integer> closeReport =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<String> shareReport =
			new MutableLiveEvent<>();
	private boolean isFeedback;
	@Nullable
	private String initialComment;

	@Inject
	ReportViewModel(@NonNull Application application,
			NetworkUsageMetrics networkUsageMetrics,
			CachingLogHandler logHandler,
			LogDecrypter logDecrypter) {
		super(application);
		collector = new NoConnectReportCollector(application, networkUsageMetrics);
		this.logHandler = logHandler;
		this.logDecrypter = logDecrypter;
	}

	void init(@Nullable Throwable t, long appStartTime,
			@Nullable byte[] logKey, @Nullable String initialComment,
			MemoryStats memoryStats) {
		this.initialComment = initialComment;
		isFeedback = t == null;
		if (reportData.getValue() == null) new SingleShotAndroidExecutor(() -> {
			String decryptedLogs;
			if (isFeedback) {
				Formatter formatter = new BriefLogFormatter();
				decryptedLogs =
						formatLog(formatter, logHandler.getRecentLogRecords());
			} else {
				decryptedLogs = logDecrypter.decryptLogs(logKey);
				if (decryptedLogs == null) {
					// error decrypting logs, get logs from this process
					Formatter formatter = new BriefLogFormatter();
					decryptedLogs = formatLog(formatter,
							logHandler.getRecentLogRecords());
				}
			}
			ReportData data = collector.collectReportData(t, appStartTime,
					decryptedLogs, memoryStats);
			reportData.postValue(data);
		}).start();
	}

	@Nullable
	String getInitialComment() {
		return initialComment;
	}

	boolean isFeedback() {
		return isFeedback;
	}

	/**
	 * Call this from the crash screen, if the user wants to report a crash.
	 */
	@UiThread
	void showReport() {
		showReport.setEvent(true);
	}

	/**
	 * Will be set to true when the user wants to report a crash.
	 */
	LiveEvent<Boolean> getShowReport() {
		return showReport;
	}

	/**
	 * The report data will be made visible in the UI when visible is true,
	 * otherwise hidden.
	 */
	@UiThread
	void showReportData(boolean visible) {
		showReportData.setValue(visible);
	}

	/**
	 * Will be set to true when the user wants to see report data.
	 */
	LiveData<Boolean> getShowReportData() {
		return showReportData;
	}

	/**
	 * The content of the report that will be loaded after
	 * {@link #init(Throwable, long, byte[], String, MemoryStats)} was called.
	 */
	LiveData<ReportData> getReportData() {
		return reportData;
	}

	/**
	 * Builds the report and hands it to the UI for sharing. Reports are never
	 * sent over the network by the app; the user chooses where to share them.
	 */
	@UiThread
	void sendReport(String comment, String email, boolean includeReport) {
		ReportData data = requireNonNull(reportData.getValue());
		if (!isNullOrEmpty(comment) || isNullOrEmpty(email)) {
			MultiReportInfo userInfo = new MultiReportInfo();
			if (!isNullOrEmpty(comment)) userInfo.add("Comment", comment);
			if (!isNullOrEmpty(email)) userInfo.add("Email", email);
			data.add(new ReportItem("UserInfo", R.string.dev_report_user_info,
					userInfo, false));
		}
		try {
			shareReport.setEvent(data.toJson(includeReport).toString(2));
		} catch (JSONException e) {
			logException(LOG, WARNING, e);
			closeReport.setEvent(R.string.dev_report_error);
		}
	}

	/**
	 * The report text, ready to be shared, after
	 * {@link #sendReport(String, String, boolean)} was called.
	 */
	LiveEvent<String> getShareReport() {
		return shareReport;
	}

	@UiThread
	void closeReport() {
		closeReport.setEvent(0);
	}

	/**
	 * An integer representing a string resource
	 * informing about the outcome of the report
	 * or 0 if no information is required, such as when back button was pressed.
	 */
	LiveEvent<Integer> getCloseReport() {
		return closeReport;
	}

	// Used for a new thread as the Android executor thread may have died
	private static class SingleShotAndroidExecutor extends Thread {

		private final Runnable runnable;

		private SingleShotAndroidExecutor(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			Looper.prepare();
			Handler handler = new Handler();
			handler.post(runnable);
			handler.post(() -> {
				Looper looper = Looper.myLooper();
				if (looper != null) looper.quit();
			});
			Looper.loop();
		}
	}

}
