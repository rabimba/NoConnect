package org.chagolchana.noconnect.android.introduction;

import org.chagolchana.noconnect.android.viewmodel.ViewModelKey;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class IntroductionModule {

	@Binds
	@IntoMap
	@ViewModelKey(IntroductionViewModel.class)
	abstract ViewModel bindIntroductionViewModel(
			IntroductionViewModel introductionViewModel);

}
