package org.chagolchana.noconnect.android.blog;

import org.chagolchana.noconnect.android.viewmodel.ViewModelKey;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface BlogModule {

	@Binds
	@IntoMap
	@ViewModelKey(FeedViewModel.class)
	ViewModel bindFeedViewModel(FeedViewModel feedViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(BlogViewModel.class)
	ViewModel bindBlogViewModel(BlogViewModel blogViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(RssFeedViewModel.class)
	ViewModel bindRssFeedViewModel(RssFeedViewModel rssFeedViewModel);
}
