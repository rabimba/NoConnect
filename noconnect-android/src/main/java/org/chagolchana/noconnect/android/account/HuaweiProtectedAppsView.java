package org.chagolchana.noconnect.android.account;


import android.content.Context;
import android.util.AttributeSet;

import org.chagolchana.noconnect.R;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import static org.chagolchana.dontkillme.HuaweiUtils.getHuaweiProtectedAppsIntent;
import static org.chagolchana.dontkillme.HuaweiUtils.protectedAppsNeedsToBeShown;
import static org.chagolchana.noconnect.android.util.UiUtils.tryToStartActivity;

@UiThread
@NotNullByDefault
class HuaweiProtectedAppsView extends PowerView {

	public HuaweiProtectedAppsView(Context context) {
		this(context, null);
	}

	public HuaweiProtectedAppsView(Context context,
			@Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HuaweiProtectedAppsView(Context context,
			@Nullable AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setText(R.string.dnkm_huawei_protected_text);
		setButtonText(R.string.dnkm_huawei_protected_button);
	}

	@Override
	public boolean needsToBeShown() {
		return protectedAppsNeedsToBeShown(getContext());
	}

	@Override
	@StringRes
	protected int getHelpText() {
		return R.string.dnkm_huawei_protected_help;
	}

	@Override
	protected void onButtonClick() {
		tryToStartActivity(getContext(), getHuaweiProtectedAppsIntent());
		setChecked(true);
	}
}
