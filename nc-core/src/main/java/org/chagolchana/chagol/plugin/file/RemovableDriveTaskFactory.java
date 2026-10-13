package org.chagolchana.chagol.plugin.file;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.plugin.file.RemovableDriveTask;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RemovableDriveTaskFactory {

	RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p);

	RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p);
}
