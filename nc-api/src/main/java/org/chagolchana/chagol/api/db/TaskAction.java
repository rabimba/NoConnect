package org.chagolchana.chagol.api.db;

import org.chagolchana.chagol.api.event.EventExecutor;

/**
 * A {@link CommitAction} that submits a task to the {@link EventExecutor}.
 */
public class TaskAction implements CommitAction {

	private final Runnable task;

	TaskAction(Runnable task) {
		this.task = task;
	}

	public Runnable getTask() {
		return task;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
