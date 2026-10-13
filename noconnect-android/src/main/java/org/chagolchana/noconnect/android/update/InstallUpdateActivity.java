package org.chagolchana.noconnect.android.update;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.activity.NoConnectActivity;
import org.chagolchana.noconnect.android.update.UpdateCheck.Result;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument;
import androidx.appcompat.app.AlertDialog;

import static android.os.Build.VERSION.SDK_INT;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.StringUtils.toHexString;

/**
 * Installs a NoConnect update from an APK file the user picks, for example one
 * received from a contact's "Share this app offline" hotspot or a USB stick.
 * The file must be NoConnect, newer than this build and signed with the same
 * certificate; the system installer then asks the user to confirm.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class InstallUpdateActivity extends NoConnectActivity {

	private static final Logger LOG =
			getLogger(InstallUpdateActivity.class.getName());

	private static final String APK_MIME_TYPE =
			"application/vnd.android.package-archive";
	private static final String ACTION_INSTALL_STATUS =
			"org.chagolchana.noconnect.android.update.INSTALL_STATUS";

	private final ActivityResultLauncher<String[]> pickApk =
			registerForActivityResult(new OpenDocument(), this::onApkPicked);

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		if (state == null) pickApk.launch(new String[] {APK_MIME_TYPE});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (!ACTION_INSTALL_STATUS.equals(intent.getAction())) return;
		int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
				PackageInstaller.STATUS_FAILURE);
		if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
			// The system installer asks the user to confirm
			Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
			if (confirm != null) startActivity(confirm);
		} else if (status != PackageInstaller.STATUS_SUCCESS) {
			showMessageAndFinish(R.string.update_install_failed);
		}
	}

	private void onApkPicked(@Nullable Uri uri) {
		if (uri == null) {
			finish();
			return;
		}
		new Thread(() -> {
			File apk = new File(getCacheDir(), "update.apk");
			Result result;
			PackageInfo info = null;
			try {
				copy(uri, apk);
				info = getArchiveInfo(apk);
				result = check(info);
			} catch (IOException | NoSuchAlgorithmException e) {
				logException(LOG, WARNING, e);
				result = Result.NOT_AN_APK;
			}
			Result finalResult = result;
			PackageInfo finalInfo = info;
			runOnUiThreadUnlessDestroyed(() -> onChecked(finalResult,
					finalInfo, apk));
		}).start();
	}

	private void onChecked(Result result, @Nullable PackageInfo info,
			File apk) {
		if (result != Result.OK || info == null) {
			apk.delete();
			showMessageAndFinish(messageFor(result));
			return;
		}
		new AlertDialog.Builder(this)
				.setTitle(R.string.update_confirm_title)
				.setMessage(getString(R.string.update_confirm_message,
						info.versionName))
				.setPositiveButton(R.string.update_install,
						(d, w) -> install(apk))
				.setNegativeButton(R.string.cancel, (d, w) -> finish())
				.setOnCancelListener(d -> finish())
				.show();
	}

	private void install(File apk) {
		PackageInstaller installer =
				getPackageManager().getPackageInstaller();
		PackageInstaller.SessionParams params =
				new PackageInstaller.SessionParams(
						PackageInstaller.SessionParams.MODE_FULL_INSTALL);
		params.setAppPackageName(getPackageName());
		try {
			int sessionId = installer.createSession(params);
			try (PackageInstaller.Session session =
					installer.openSession(sessionId)) {
				try (InputStream in = new java.io.FileInputStream(apk);
						OutputStream out = session.openWrite("update", 0,
								apk.length())) {
					byte[] buf = new byte[64 * 1024];
					int read;
					while ((read = in.read(buf)) != -1) out.write(buf, 0, read);
					session.fsync(out);
				}
				Intent status = new Intent(this, InstallUpdateActivity.class)
						.setAction(ACTION_INSTALL_STATUS)
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				// Mutable so the installer can add its status extras
				int flags = PendingIntent.FLAG_UPDATE_CURRENT
						| (SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
				PendingIntent pi = PendingIntent.getActivity(this, sessionId,
						status, flags);
				session.commit(pi.getIntentSender());
			}
		} catch (IOException | SecurityException e) {
			logException(LOG, WARNING, e);
			showMessageAndFinish(R.string.update_install_failed);
		} finally {
			apk.delete();
		}
	}

	private void copy(Uri uri, File dest) throws IOException {
		try (InputStream in = getContentResolver().openInputStream(uri);
				OutputStream out = new FileOutputStream(dest)) {
			if (in == null) throw new IOException();
			byte[] buf = new byte[64 * 1024];
			int read;
			while ((read = in.read(buf)) != -1) out.write(buf, 0, read);
		}
	}

	@Nullable
	@SuppressWarnings("deprecation")
	private PackageInfo getArchiveInfo(File apk) {
		int flags = SDK_INT >= 28 ? PackageManager.GET_SIGNING_CERTIFICATES
				: PackageManager.GET_SIGNATURES;
		return getPackageManager().getPackageArchiveInfo(apk.getPath(), flags);
	}

	@SuppressWarnings("deprecation")
	private Result check(@Nullable PackageInfo apkInfo)
			throws NoSuchAlgorithmException {
		PackageInfo ours;
		try {
			int flags = SDK_INT >= 28
					? PackageManager.GET_SIGNING_CERTIFICATES
					: PackageManager.GET_SIGNATURES;
			ours = getPackageManager().getPackageInfo(getPackageName(), flags);
		} catch (PackageManager.NameNotFoundException e) {
			throw new AssertionError(e);
		}
		if (apkInfo == null) {
			return UpdateCheck.check(null, 0, new HashSet<>(),
					getPackageName(), versionCode(ours), signers(ours));
		}
		return UpdateCheck.check(apkInfo.packageName, versionCode(apkInfo),
				signers(apkInfo), getPackageName(), versionCode(ours),
				signers(ours));
	}

	@SuppressWarnings("deprecation")
	private static long versionCode(PackageInfo info) {
		return SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
	}

	@SuppressWarnings("deprecation")
	private static Set<String> signers(PackageInfo info)
			throws NoSuchAlgorithmException {
		Signature[] signatures;
		if (SDK_INT >= 28) {
			signatures = info.signingInfo == null ? null
					: info.signingInfo.getApkContentsSigners();
		} else {
			signatures = info.signatures;
		}
		Set<String> digests = new HashSet<>();
		if (signatures == null) return digests;
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		for (Signature s : signatures) {
			digests.add(toHexString(sha256.digest(s.toByteArray())));
		}
		return digests;
	}

	private static int messageFor(Result result) {
		switch (result) {
			case WRONG_APP:
				return R.string.update_wrong_app;
			case NOT_NEWER:
				return R.string.update_not_newer;
			case WRONG_SIGNER:
				return R.string.update_wrong_signer;
			default:
				return R.string.update_not_an_apk;
		}
	}

	private void showMessageAndFinish(int res) {
		Toast.makeText(this, res, LENGTH_LONG).show();
		finish();
	}
}
