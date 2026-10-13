package org.chagolchana.noconnect.android.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.chagolchana.noconnect.BuildConfig;
import org.chagolchana.noconnect.R;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Intent.ACTION_VIEW;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.noconnect.android.util.UiUtils.tryToStartActivity;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AboutFragment extends Fragment {

	final static String TAG = AboutFragment.class.getName();
	private static final Logger LOG = getLogger(TAG);

	private TextView noConnectVersion;
	private TextView torVersion;
	private TextView noConnectWebsite;
	private TextView noConnectSourceCode;
	private TextView noConnectChangelog;
	private TextView noConnectPrivacyPolicy;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container,
				false);
	}

	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.about_title);
		noConnectVersion = requireActivity().findViewById(R.id.NoConnectVersion);
		noConnectVersion.setText(
				getString(R.string.nc_version, BuildConfig.VERSION_NAME));
		torVersion = requireActivity().findViewById(R.id.TorVersion);
		torVersion.setText(
				getString(R.string.tor_version, BuildConfig.TorVersion));
		noConnectWebsite = requireActivity().findViewById(R.id.NoConnectWebsite);
		noConnectSourceCode = requireActivity().findViewById(R.id.NoConnectSourceCode);
		noConnectChangelog = requireActivity().findViewById(R.id.NoConnectChangelog);
		noConnectPrivacyPolicy =
				requireActivity().findViewById(R.id.NoConnectPrivacyPolicy);
		noConnectWebsite.setOnClickListener(View -> {
			String url = "https://github.com/rabimba/noconnect";
			goToUrl(url);
		});
		noConnectSourceCode.setOnClickListener(View -> {
			String url = "https://github.com/rabimba/noconnect";
			goToUrl(url);
		});
		noConnectChangelog.setOnClickListener(View -> {
			String url =
					"https://github.com/rabimba/noconnect";
			goToUrl(url);
		});
		noConnectPrivacyPolicy.setOnClickListener(View -> {
			String url =
					"https://github.com/rabimba/noconnect";
			goToUrl(url);
		});
	}

	private void goToUrl(String url) {
		Intent i = new Intent(ACTION_VIEW);
		i.setData(Uri.parse(url));
		tryToStartActivity(requireActivity(), i);
	}

}