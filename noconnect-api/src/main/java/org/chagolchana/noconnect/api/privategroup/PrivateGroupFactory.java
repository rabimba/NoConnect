package org.chagolchana.noconnect.api.privategroup;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.sync.Group;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PrivateGroupFactory {

	/**
	 * Creates a private group with the given name and author.
	 */
	PrivateGroup createPrivateGroup(String name, Author creator);

	/**
	 * Creates a private group with the given name, author and salt.
	 */
	PrivateGroup createPrivateGroup(String name, Author creator, byte[] salt);

	/**
	 * Parses a group and returns the corresponding PrivateGroup.
	 */
	PrivateGroup parsePrivateGroup(Group group) throws FormatException;

}
