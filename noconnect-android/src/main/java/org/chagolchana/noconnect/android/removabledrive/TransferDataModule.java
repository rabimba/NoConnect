package org.chagolchana.noconnect.android.removabledrive;

import org.chagolchana.noconnect.android.viewmodel.ViewModelKey;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface TransferDataModule {

	@Binds
	@IntoMap
	@ViewModelKey(RemovableDriveViewModel.class)
	ViewModel bindRemovableDriveViewModel(
			RemovableDriveViewModel removableDriveViewModel);

}
