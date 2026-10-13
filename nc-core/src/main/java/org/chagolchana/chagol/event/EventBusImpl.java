package org.chagolchana.chagol.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventExecutor;
import org.chagolchana.chagol.api.event.EventListener;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

@ThreadSafe
@NotNullByDefault
class EventBusImpl implements EventBus {

	private final Collection<EventListener> listeners =
			new CopyOnWriteArrayList<>();
	private final Executor eventExecutor;

	@Inject
	EventBusImpl(@EventExecutor Executor eventExecutor) {
		this.eventExecutor = eventExecutor;
	}

	@Override
	public void addListener(EventListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(EventListener l) {
		listeners.remove(l);
	}

	@Override
	public void broadcast(Event e) {
		eventExecutor.execute(() -> {
			for (EventListener l : listeners) l.eventOccurred(e);
		});
	}
}
