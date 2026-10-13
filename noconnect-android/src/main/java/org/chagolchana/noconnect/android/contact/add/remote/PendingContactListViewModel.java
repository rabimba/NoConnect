package org.chagolchana.noconnect.android.contact.add.remote;

import android.app.Application;

import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.contact.PendingContact;
import org.chagolchana.chagol.api.contact.PendingContactId;
import org.chagolchana.chagol.api.contact.PendingContactState;
import org.chagolchana.chagol.api.contact.event.PendingContactRemovedEvent;
import org.chagolchana.chagol.api.contact.event.PendingContactStateChangedEvent;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventListener;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.rendezvous.RendezvousPoller;
import org.chagolchana.chagol.api.rendezvous.event.RendezvousPollEvent;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.android.viewmodel.DbViewModel;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static org.chagolchana.chagol.api.contact.PendingContactState.OFFLINE;

@NotNullByDefault
public class PendingContactListViewModel extends DbViewModel
		implements EventListener {

	private final ContactManager contactManager;
	private final RendezvousPoller rendezvousPoller;
	private final EventBus eventBus;

	private final MutableLiveData<Collection<PendingContactItem>>
			pendingContacts = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasInternetConnection =
			new MutableLiveData<>();

	@Inject
	PendingContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			ContactManager contactManager,
			RendezvousPoller rendezvousPoller,
			EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
		this.rendezvousPoller = rendezvousPoller;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}

	void onCreate() {
		if (pendingContacts.getValue() == null) loadPendingContacts();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactStateChangedEvent ||
				e instanceof PendingContactRemovedEvent ||
				e instanceof RendezvousPollEvent) {
			loadPendingContacts();
		}
	}

	private void loadPendingContacts() {
		runOnDbThread(() -> {
			try {
				Collection<Pair<PendingContact, PendingContactState>> pairs =
						contactManager.getPendingContacts();
				List<PendingContactItem> items = new ArrayList<>(pairs.size());
				boolean online = pairs.isEmpty();
				for (Pair<PendingContact, PendingContactState> pair : pairs) {
					PendingContact p = pair.getFirst();
					PendingContactState state = pair.getSecond();
					long lastPoll = rendezvousPoller.getLastPollTime(p.getId());
					items.add(new PendingContactItem(p, state, lastPoll));
					online = online || state != OFFLINE;
				}
				pendingContacts.postValue(items);
				hasInternetConnection.postValue(online);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Collection<PendingContactItem>> getPendingContacts() {
		return pendingContacts;
	}

	void removePendingContact(PendingContactId id) {
		runOnDbThread(() -> {
			try {
				contactManager.removePendingContact(id);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Boolean> getHasInternetConnection() {
		return hasInternetConnection;
	}

}
