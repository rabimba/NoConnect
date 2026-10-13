package org.chagolchana.noconnect.android.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.identity.LocalAuthor;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.noconnect.android.AndroidComponent;
import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import static java.util.Collections.singletonMap;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.api.plugin.BleConstants.ID;
import static org.chagolchana.chagol.api.plugin.BleConstants.PROP_SECRET;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.StringUtils.fromHexString;
import static org.chagolchana.chagol.util.StringUtils.toHexString;

/**
 * DEBUG BUILDS ONLY (lives in the debug source set). Lets two emulators
 * become contacts without scanning each other's QR codes, so transports such
 * as Bluetooth LE can be tested end to end:
 * <pre>
 * adb shell am broadcast -n PKG/org.chagolchana.noconnect.android.test.TestContactReceiver \
 *     -a org.chagolchana.noconnect.android.test.DUMP_IDENTITY
 * adb shell am broadcast -n PKG/... -a org.chagolchana.noconnect.android.test.ADD_CONTACT \
 *     --es name NAME --es pub HEX --es ble HEX --es root HEX --el time MS --ez alice true|false
 * </pre>
 * Both sides must use the same root key and time and opposite alice values.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class TestContactReceiver extends BroadcastReceiver {

	private static final Logger LOG =
			getLogger(TestContactReceiver.class.getName());

	private static final String PREFIX =
			"org.chagolchana.noconnect.android.test.";
	private static final String ACTION_DUMP = PREFIX + "DUMP_IDENTITY";
	private static final String ACTION_ADD = PREFIX + "ADD_CONTACT";

	@Override
	public void onReceive(Context ctx, Intent intent) {
		AndroidComponent c = ((NoConnectApplication) ctx.getApplicationContext())
				.getApplicationComponent();
		PendingResult result = goAsync();
		c.databaseExecutor().execute(() -> {
			try {
				if (ACTION_DUMP.equals(intent.getAction())) dump(c);
				else if (ACTION_ADD.equals(intent.getAction())) add(c, intent);
			} catch (Exception e) {
				logException(LOG, WARNING, e);
			} finally {
				result.finish();
			}
		});
	}

	private void dump(AndroidComponent c) throws Exception {
		LocalAuthor a = c.identityManager().getLocalAuthor();
		String ble = c.transportPropertyManager().getLocalProperties(ID)
				.get(PROP_SECRET);
		LOG.info("TEST_IDENTITY name=" + a.getName() + " pub="
				+ toHexString(a.getPublicKey().getEncoded()) + " ble=" + ble);
	}

	private void add(AndroidComponent c, Intent i)
			throws FormatException, GeneralSecurityException, Exception {
		Author remote = c.authorFactory().createAuthor(i.getStringExtra("name"),
				c.cryptoComponent().getSignatureKeyParser().parsePublicKey(
						fromHexString(i.getStringExtra("pub"))));
		SecretKey root = new SecretKey(fromHexString(i.getStringExtra("root")));
		long time = i.getLongExtra("time", 0);
		boolean alice = i.getBooleanExtra("alice", false);
		TransportProperties p = new TransportProperties();
		p.put(PROP_SECRET, i.getStringExtra("ble"));
		LocalAuthor local = c.identityManager().getLocalAuthor();
		ContactId id = c.transactionManager().transactionWithResult(false,
				txn -> {
					ContactId contactId = c.contactManager().addContact(txn,
							remote, local.getId(), root, time, alice, true,
							true);
					c.transportPropertyManager().addRemoteProperties(txn,
							contactId, singletonMap(ID, p));
					return contactId;
				});
		LOG.info("TEST_CONTACT_ADDED " + id.getInt());
	}
}
