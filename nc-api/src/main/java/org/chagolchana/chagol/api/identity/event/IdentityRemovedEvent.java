package org.chagolchana.chagol.api.identity.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.identity.AuthorId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when an identity is removed.
 */
@Immutable
@NotNullByDefault
public class IdentityRemovedEvent extends Event {

	private final AuthorId authorId;

	public IdentityRemovedEvent(AuthorId authorId) {
		this.authorId = authorId;
	}

	public AuthorId getAuthorId() {
		return authorId;
	}
}
