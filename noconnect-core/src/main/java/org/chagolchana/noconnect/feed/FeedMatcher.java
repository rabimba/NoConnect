package org.chagolchana.noconnect.feed;

import org.chagolchana.noconnect.api.feed.Feed;
import org.chagolchana.noconnect.api.feed.RssProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface FeedMatcher {

	/**
	 * Returns the best match for the given candidate from the given list of
	 * feeds, or null if there are no matches.
	 */
	@Nullable
	Feed findMatchingFeed(RssProperties candidate, List<Feed> feeds);
}
