package org.chagolchana.chagol.plugin.file;

import org.chagolchana.chagol.api.plugin.file.RemovableDriveTask;
import org.chagolchana.nullsafety.NotNullByDefault;

@Deprecated // We can simply remove tasks when they finish
@NotNullByDefault
interface RemovableDriveTaskRegistry {

	void removeReader(RemovableDriveTask task);

	void removeWriter(RemovableDriveTask task);
}
