package org.chagolchana.chagol.api.keyagreement;

import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class TransportDescriptor {

	private final TransportId id;
	private final BdfList descriptor;

	public TransportDescriptor(TransportId id, BdfList descriptor) {
		this.id = id;
		this.descriptor = descriptor;
	}

	public TransportId getId() {
		return id;
	}

	public BdfList getDescriptor() {
		return descriptor;
	}
}
