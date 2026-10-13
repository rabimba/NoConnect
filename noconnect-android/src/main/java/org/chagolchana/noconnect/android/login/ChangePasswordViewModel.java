package org.chagolchana.noconnect.android.login;

import org.chagolchana.chagol.api.account.AccountManager;
import org.chagolchana.chagol.api.crypto.DecryptionException;
import org.chagolchana.chagol.api.crypto.DecryptionResult;
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.noconnect.android.viewmodel.LiveEvent;
import org.chagolchana.noconnect.android.viewmodel.MutableLiveEvent;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidx.lifecycle.ViewModel;

import static org.chagolchana.chagol.api.crypto.DecryptionResult.SUCCESS;

@NotNullByDefault
public class ChangePasswordViewModel extends ViewModel {

	private final AccountManager accountManager;
	private final Executor ioExecutor;
	private final PasswordStrengthEstimator strengthEstimator;

	@Inject
	ChangePasswordViewModel(AccountManager accountManager,
			@IoExecutor Executor ioExecutor,
			PasswordStrengthEstimator strengthEstimator) {
		this.accountManager = accountManager;
		this.ioExecutor = ioExecutor;
		this.strengthEstimator = strengthEstimator;
	}

	float estimatePasswordStrength(String password) {
		return strengthEstimator.estimateStrength(password);
	}

	LiveEvent<DecryptionResult> changePassword(String oldPassword,
			String newPassword) {
		MutableLiveEvent<DecryptionResult> result = new MutableLiveEvent<>();
		ioExecutor.execute(() -> {
			try {
				accountManager.changePassword(oldPassword, newPassword);
				result.postEvent(SUCCESS);
			} catch (DecryptionException e) {
				result.postEvent(e.getDecryptionResult());
			}
		});
		return result;
	}
}
