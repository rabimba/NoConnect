package org.chagolchana.noconnect.android.navdrawer;

import android.app.Application;

import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.noconnect.android.viewmodel.DbViewModel;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static org.chagolchana.noconnect.BuildConfig.BuildTimestamp;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.dontkillme.DozeUtils.needsDozeWhitelisting;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.noconnect.android.TestingConstants.EXPIRY_DATE;
import static org.chagolchana.noconnect.android.controller.NoConnectControllerImpl.DOZE_ASK_AGAIN;
import static org.chagolchana.noconnect.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@NotNullByDefault
public class NavDrawerViewModel extends DbViewModel {

	private static final Logger LOG =
			getLogger(NavDrawerViewModel.class.getName());

	private static final String EXPIRY_DATE_WARNING = "expiryDateWarning";
	private static final String UPDATE_REMINDER_DISMISSED =
			"updateReminderDismissed";
	private static final String SHOW_TRANSPORTS_ONBOARDING =
			"showTransportsOnboarding";

	private final SettingsManager settingsManager;

	private final MutableLiveData<Boolean> showExpiryWarning =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> shouldAskForDozeWhitelisting =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> showTransportsOnboarding =
			new MutableLiveData<>();

	@Inject
	NavDrawerViewModel(Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
	}

	private final MutableLiveData<Integer> showUpdateReminder =
			new MutableLiveData<>();

	/**
	 * Emits the build's age in months when the user should be reminded to
	 * update, or null when the reminder should be hidden.
	 */
	LiveData<Integer> showUpdateReminder() {
		return showUpdateReminder;
	}

	@UiThread
	void checkUpdateReminder() {
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				long lastDismissed =
						settings.getLong(UPDATE_REMINDER_DISMISSED, 0);
				long now = System.currentTimeMillis();
				if (BuildAgePolicy.shouldShowReminder(now, BuildTimestamp,
						lastDismissed)) {
					showUpdateReminder.postValue(
							BuildAgePolicy.monthsOld(now, BuildTimestamp));
				} else {
					showUpdateReminder.postValue(null);
				}
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@UiThread
	void updateReminderDismissed() {
		showUpdateReminder.setValue(null);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				settings.putLong(UPDATE_REMINDER_DISMISSED,
						System.currentTimeMillis());
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Boolean> showExpiryWarning() {
		return showExpiryWarning;
	}

	@UiThread
	void checkExpiryWarning() {
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				int warningInt = settings.getInt(EXPIRY_DATE_WARNING, 0);

				if (warningInt == 0) {
					// we have not warned before
					showExpiryWarning.postValue(true);
				} else {
					long warningLong = warningInt * 1000L;
					long now = System.currentTimeMillis();
					long daysSinceLastWarning =
							(now - warningLong) / DAYS.toMillis(1);
					long daysBeforeExpiry =
							(EXPIRY_DATE - now) / DAYS.toMillis(1);

					if (daysSinceLastWarning >= 30) {
						showExpiryWarning.postValue(true);
					} else if (daysBeforeExpiry <= 3 &&
							daysSinceLastWarning > 0) {
						showExpiryWarning.postValue(true);
					} else {
						showExpiryWarning.postValue(false);
					}
				}
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@UiThread
	void expiryWarningDismissed() {
		showExpiryWarning.setValue(false);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				int date = (int) (System.currentTimeMillis() / 1000L);
				settings.putInt(EXPIRY_DATE_WARNING, date);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Boolean> shouldAskForDozeWhitelisting() {
		return shouldAskForDozeWhitelisting;
	}

	@UiThread
	void checkDozeWhitelisting() {
		// check this first, to hit the DbThread only when really necessary
		NoConnectApplication app = getApplication();
		if (app.isInstrumentationTest() ||
				!needsDozeWhitelisting(getApplication())) {
			shouldAskForDozeWhitelisting.setValue(false);
			return;
		}
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				shouldAskForDozeWhitelisting.postValue(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				shouldAskForDozeWhitelisting.postValue(true);
			}
		});
	}

	@UiThread
	LiveData<Boolean> showTransportsOnboarding() {
		return showTransportsOnboarding;
	}

	@UiThread
	void checkTransportsOnboarding() {
		if (showTransportsOnboarding.getValue() != null) return;
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean show =
						settings.getBoolean(SHOW_TRANSPORTS_ONBOARDING, true);
				showTransportsOnboarding.postValue(show);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@UiThread
	void transportsOnboardingShown() {
		showTransportsOnboarding.setValue(false);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(SHOW_TRANSPORTS_ONBOARDING, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
}
