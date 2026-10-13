package org.chagolchana.noconnect.android.contact;

import org.chagolchana.noconnect.android.viewmodel.ViewModelKey;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ContactListModule {

	@Binds
	@IntoMap
	@ViewModelKey(ContactListViewModel.class)
	abstract ViewModel bindContactListViewModel(
			ContactListViewModel contactListViewModel);

}
