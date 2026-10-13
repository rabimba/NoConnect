package org.chagolchana.noconnect.android.privategroup.conversation;

import org.chagolchana.noconnect.android.viewmodel.ViewModelKey;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface GroupConversationModule {

	@Binds
	@IntoMap
	@ViewModelKey(GroupViewModel.class)
	ViewModel bindGroupViewModel(GroupViewModel groupViewModel);

}
