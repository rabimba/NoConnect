package org.chagolchana.noconnect.feed;

import org.chagolchana.chagol.api.identity.Identity;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.chagolchana.chagol.test.TestDatabaseConfigModule;
import org.chagolchana.noconnect.api.blog.Blog;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.BlogPostHeader;
import org.chagolchana.noconnect.api.feed.Feed;
import org.chagolchana.noconnect.api.feed.FeedManager;
import org.chagolchana.nullsafety.NullSafety;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import javax.annotation.Nullable;

import static org.chagolchana.chagol.test.TestUtils.deleteTestDirectory;
import static org.chagolchana.chagol.test.TestUtils.getSecretKey;
import static org.chagolchana.chagol.test.TestUtils.getTestDirectory;
import static org.chagolchana.nullsafety.NullSafety.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FeedManagerIntegrationTest extends ChagolTestCase {

	private static final String FEED_PATH =
			"src/test/resources/noconnectproject.org_news_index.xml";
	private static final String FEED_URL =
			"https://github.com/rabimba/noconnect";
	private static final String FEED_TITLE = "News - NoConnect";

	private LifecycleManager lifecycleManager;
	private FeedManager feedManager;
	private BlogManager blogManager;
	private final File testDir = getTestDirectory();
	private final File testFile = new File(testDir, "feedTest");

	@Before
	public void setUp() throws Exception {
		assertTrue(testDir.mkdirs());
		FeedManagerIntegrationTestComponent component =
				DaggerFeedManagerIntegrationTestComponent.builder()
						.testDatabaseConfigModule(
								new TestDatabaseConfigModule(testFile)).build();
		FeedManagerIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);

		IdentityManager identityManager = component.getIdentityManager();
		Identity identity = identityManager.createIdentity("feedTest");
		identityManager.registerIdentity(identity);

		lifecycleManager = component.getLifecycleManager();
		lifecycleManager.startServices(getSecretKey());
		lifecycleManager.waitForStartup();

		feedManager = component.getFeedManager();
		blogManager = component.getBlogManager();
	}

	@Test
	public void testFeedImportAndRemovalFromUrl() throws Exception {
		testFeedImportAndRemoval(FEED_URL, null);
	}

	@Test
	public void testFeedImportAndRemovalFromFile() throws Exception {
		testFeedImportAndRemoval(null, FEED_PATH);
	}

	private void testFeedImportAndRemoval(@Nullable String url,
			@Nullable String path) throws Exception {
		// initially, there's only the one personal blog
		Collection<Blog> blogs = blogManager.getBlogs();
		assertEquals(1, blogs.size());
		Blog personalBlog = blogs.iterator().next();

		// add feed into a dedicated blog
		if (url == null) {
			feedManager.addFeed(new FileInputStream(requireNonNull(path)));
		} else {
			feedManager.addFeed(url);
		}

		// now there's the feed's blog too
		blogs = blogManager.getBlogs();
		assertEquals(2, blogs.size());
		Blog feedBlog = null;
		for (Blog blog : blogs) {
			if (!blog.equals(personalBlog)) feedBlog = blog;
		}
		assertNotNull(feedBlog);

		// check the feed got saved as expected
		Collection<Feed> feeds = feedManager.getFeeds();
		assertEquals(1, feeds.size());
		Feed feed = feeds.iterator().next();
		assertTrue(feed.getLastEntryTime() > 0);
		assertTrue(feed.getAdded() > 0);
		assertTrue(feed.getUpdated() > 0);
		assertTrue(NullSafety.equals(url, feed.getProperties().getUrl()));
		assertEquals(feedBlog, feed.getBlog());
		assertEquals(FEED_TITLE, feed.getTitle());
		assertEquals(feed.getTitle(), feed.getBlog().getName());
		assertEquals(feed.getTitle(), feed.getLocalAuthor().getName());

		// check the feed entries have been added to the blog as expected
		Collection<BlogPostHeader> headers =
				blogManager.getPostHeaders(feedBlog.getId());
		assertFalse(headers.isEmpty());
		for (BlogPostHeader header : headers) {
			assertTrue(header.isRssFeed());
		}

		// now let's remove the feed's blog again
		blogManager.removeBlog(feedBlog);
		blogs = blogManager.getBlogs();
		assertEquals(1, blogs.size());
		assertEquals(personalBlog, blogs.iterator().next());
		assertEquals(0, feedManager.getFeeds().size());
	}

	@After
	public void tearDown() throws Exception {
		lifecycleManager.stopServices();
		lifecycleManager.waitForShutdown();
		deleteTestDirectory(testDir);
	}
}
