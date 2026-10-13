package org.chagolchana.noconnect.android.controller;

import org.chagolchana.nullsafety.NotNullByDefault;

@Deprecated
@NotNullByDefault
public interface DbController {

	void runOnDbThread(Runnable task);
}
