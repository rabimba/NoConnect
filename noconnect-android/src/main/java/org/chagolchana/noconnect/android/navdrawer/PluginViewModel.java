package org.chagolchana.noconnect.android.navdrawer;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventListener;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.network.NetworkManager;
import org.chagolchana.chagol.api.network.NetworkStatus;
import org.chagolchana.chagol.api.network.event.NetworkStatusEvent;
import org.chagolchana.chagol.api.plugin.BleConstants;
import org.chagolchana.chagol.api.plugin.BluetoothConstants;
import org.chagolchana.chagol.api.plugin.LanTcpConstants;
import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.Plugin.State;
import org.chagolchana.chagol.api.plugin.PluginManager;
import org.chagolchana.chagol.api.plugin.TorConstants;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.event.TransportStateEvent;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.chagol.api.settings.event.SettingsUpdatedEvent;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.android.viewmodel.DbViewModel;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static org.chagolchana.chagol.api.plugin.Plugin.State.STARTING_STOPPING;
import static org.chagolchana.chagol.util.AndroidUtils.registerReceiver;
import static org.chagolchana.chagol.util.LogUtils.logDuration;
import static org.chagolchana.chagol.util.LogUtils.now;

@NotNullByDefault
public class PluginViewModel extends DbViewModel implements EventListener {

	private static final Logger LOG =
			getLogger(PluginViewModel.class.getName());

	private final Application app;
	private final SettingsManager settingsManager;
	private final PluginManager pluginManager;
	private final EventBus eventBus;
	private final BroadcastReceiver receiver;

	private final MutableLiveData<State> torPluginState =
			new MutableLiveData<>();
	private final MutableLiveData<State> wifiPluginState =
			new MutableLiveData<>();
	private final MutableLiveData<State> btPluginState =
			new MutableLiveData<>();
	private final MutableLiveData<State> blePluginState =
			new MutableLiveData<>();

	private final MutableLiveData<Boolean> torEnabledSetting =
			new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> wifiEnabledSetting =
			new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> btEnabledSetting =
			new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> bleEnabledSetting =
			new MutableLiveData<>(false);

	private final MutableLiveData<NetworkStatus> networkStatus =
			new MutableLiveData<>();

	private final MutableLiveData<Boolean> bluetoothTurnedOn =
			new MutableLiveData<>(false);

	@Inject
	PluginViewModel(Application app, @DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, SettingsManager settingsManager,
			PluginManager pluginManager, EventBus eventBus,
			NetworkManager networkManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.app = app;
		this.settingsManager = settingsManager;
		this.pluginManager = pluginManager;
		this.eventBus = eventBus;
		eventBus.addListener(this);
		receiver = new BluetoothStateReceiver();
		registerReceiver(app, receiver, new IntentFilter(ACTION_STATE_CHANGED),
				false);
		networkStatus.setValue(networkManager.getNetworkStatus());
		torPluginState.setValue(getTransportState(TorConstants.ID));
		wifiPluginState.setValue(getTransportState(LanTcpConstants.ID));
		btPluginState.setValue(getTransportState(BluetoothConstants.ID));
		blePluginState.setValue(getTransportState(BleConstants.ID));
		initialiseBluetoothState();
		loadSettings();
	}

	@Override
	protected void onCleared() {
		eventBus.removeListener(this);
		app.unregisterReceiver(receiver);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof NetworkStatusEvent) {
			networkStatus.setValue(((NetworkStatusEvent) e).getStatus());
		} else if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			if (s.getNamespace().equals(TorConstants.ID.getString())) {
				boolean enable = s.getSettings().getBoolean(PREF_PLUGIN_ENABLE,
						TorConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				torEnabledSetting.setValue(enable);
			} else if (s.getNamespace().equals(
					LanTcpConstants.ID.getString())) {
				boolean enable = s.getSettings().getBoolean(PREF_PLUGIN_ENABLE,
						LanTcpConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				wifiEnabledSetting.setValue(enable);
			} else if (s.getNamespace().equals(
					BluetoothConstants.ID.getString())) {
				boolean enable = s.getSettings().getBoolean(PREF_PLUGIN_ENABLE,
						BluetoothConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				btEnabledSetting.setValue(enable);
			} else if (s.getNamespace().equals(
					BleConstants.ID.getString())) {
				boolean enable = s.getSettings().getBoolean(PREF_PLUGIN_ENABLE,
						BleConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				bleEnabledSetting.setValue(enable);
			}
		} else if (e instanceof TransportStateEvent) {
			TransportStateEvent t = (TransportStateEvent) e;
			TransportId id = t.getTransportId();
			State state = t.getState();
			if (LOG.isLoggable(INFO)) {
				LOG.info("TransportStateEvent: " + id + " is " + state);
			}
			MutableLiveData<State> liveData = getPluginLiveData(id);
			if (liveData != null) liveData.postValue(state);
		}
	}

	LiveData<State> getPluginState(TransportId id) {
		LiveData<State> liveData = getPluginLiveData(id);
		if (liveData == null) throw new IllegalArgumentException();
		return liveData;
	}

	LiveData<Boolean> getPluginEnabledSetting(TransportId id) {
		if (id.equals(TorConstants.ID)) return torEnabledSetting;
		else if (id.equals(LanTcpConstants.ID)) return wifiEnabledSetting;
		else if (id.equals(BluetoothConstants.ID)) return btEnabledSetting;
		else if (id.equals(BleConstants.ID)) return bleEnabledSetting;
		else throw new IllegalArgumentException();
	}

	LiveData<NetworkStatus> getNetworkStatus() {
		return networkStatus;
	}

	LiveData<Boolean> getBluetoothTurnedOn() {
		return bluetoothTurnedOn;
	}

	int getReasonsTorDisabled() {
		Plugin plugin = pluginManager.getPlugin(TorConstants.ID);
		return plugin == null ? 0 : plugin.getReasonsDisabled();
	}

	void enableTransport(TransportId id, boolean enable) {
		Settings s = new Settings();
		s.putBoolean(PREF_PLUGIN_ENABLE, enable);
		mergeSettings(s, id.getString());
	}

	private void initialiseBluetoothState() {
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		if (bt == null) bluetoothTurnedOn.setValue(false);
		else bluetoothTurnedOn.setValue(bt.getState() == STATE_ON);
	}

	private void loadSettings() {
		runOnDbThread(() -> {
			try {
				boolean tor = isPluginEnabled(TorConstants.ID,
						TorConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				torEnabledSetting.postValue(tor);
				boolean wifi = isPluginEnabled(LanTcpConstants.ID,
						LanTcpConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				wifiEnabledSetting.postValue(wifi);
				boolean bt = isPluginEnabled(BluetoothConstants.ID,
						BluetoothConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				btEnabledSetting.postValue(bt);
				boolean ble = isPluginEnabled(BleConstants.ID,
						BleConstants.DEFAULT_PREF_PLUGIN_ENABLE);
				bleEnabledSetting.postValue(ble);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	private boolean isPluginEnabled(TransportId id, boolean defaultValue)
			throws DbException {
		Settings s = settingsManager.getSettings(id.getString());
		return s.getBoolean(PREF_PLUGIN_ENABLE, defaultValue);
	}

	private State getTransportState(TransportId id) {
		Plugin plugin = pluginManager.getPlugin(id);
		return plugin == null ? STARTING_STOPPING : plugin.getState();
	}

	@Nullable
	private MutableLiveData<State> getPluginLiveData(TransportId id) {
		if (id.equals(TorConstants.ID)) return torPluginState;
		else if (id.equals(LanTcpConstants.ID)) return wifiPluginState;
		else if (id.equals(BluetoothConstants.ID)) return btPluginState;
		else if (id.equals(BleConstants.ID)) return blePluginState;
		else return null;
	}

	private void mergeSettings(Settings s, String namespace) {
		runOnDbThread(() -> {
			try {
				long start = now();
				settingsManager.mergeSettings(s, namespace);
				logDuration(LOG, "Merging settings", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	private class BluetoothStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(EXTRA_STATE, 0);
			bluetoothTurnedOn.postValue(state == STATE_ON);
		}
	}
}
