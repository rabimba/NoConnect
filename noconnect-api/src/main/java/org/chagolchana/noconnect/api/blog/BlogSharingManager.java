package org.chagolchana.noconnect.api.blog;

import org.chagolchana.chagol.api.sync.ClientId;
import org.chagolchana.noconnect.api.sharing.SharingManager;

public interface BlogSharingManager extends SharingManager<Blog> {

	/**
	 * The unique ID of the blog sharing client.
	 */
	ClientId CLIENT_ID = new ClientId("org.chagolchana.noconnect.blog.sharing");

	/**
	 * The current major version of the blog sharing client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the blog sharing client.
	 */
	int MINOR_VERSION = 1;
}
