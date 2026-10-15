package org.chagolchana.chagol;

import org.chagolchana.chagol.cleanup.CleanupModule;
import org.chagolchana.chagol.client.ClientModule;
import org.chagolchana.chagol.connection.ConnectionModule;
import org.chagolchana.chagol.contact.ContactModule;
import org.chagolchana.chagol.crypto.CryptoExecutorModule;
import org.chagolchana.chagol.crypto.CryptoModule;
import org.chagolchana.chagol.data.DataModule;
import org.chagolchana.chagol.db.DatabaseExecutorModule;
import org.chagolchana.chagol.db.DatabaseModule;
import org.chagolchana.chagol.event.EventModule;
import org.chagolchana.chagol.identity.IdentityModule;
import org.chagolchana.chagol.io.IoModule;
import org.chagolchana.chagol.keyagreement.KeyAgreementModule;
import org.chagolchana.chagol.lifecycle.LifecycleModule;
import org.chagolchana.chagol.mailbox.MailboxModule;
import org.chagolchana.chagol.plugin.PluginModule;
import org.chagolchana.chagol.properties.PropertiesModule;
import org.chagolchana.chagol.qrcode.QrCodeModule;
import org.chagolchana.chagol.record.RecordModule;
import org.chagolchana.chagol.relay.RelayModule;
import org.chagolchana.chagol.reliability.ReliabilityModule;
import org.chagolchana.chagol.rendezvous.RendezvousModule;
import org.chagolchana.chagol.settings.SettingsModule;
import org.chagolchana.chagol.sync.SyncModule;
import org.chagolchana.chagol.sync.validation.ValidationModule;
import org.chagolchana.chagol.transport.TransportModule;
import org.chagolchana.chagol.transport.agreement.TransportKeyAgreementModule;
import org.chagolchana.chagol.versioning.VersioningModule;

import dagger.Module;

@Module(includes = {
		CleanupModule.class,
		ClientModule.class,
		ConnectionModule.class,
		ContactModule.class,
		CryptoModule.class,
		CryptoExecutorModule.class,
		DataModule.class,
		DatabaseModule.class,
		DatabaseExecutorModule.class,
		EventModule.class,
		IdentityModule.class,
		IoModule.class,
		KeyAgreementModule.class,
		LifecycleModule.class,
		MailboxModule.class,
		PluginModule.class,
		PropertiesModule.class,
		QrCodeModule.class,
		RecordModule.class,
		RelayModule.class,
		ReliabilityModule.class,
		RendezvousModule.class,
		SettingsModule.class,
		SyncModule.class,
		TransportKeyAgreementModule.class,
		TransportModule.class,
		ValidationModule.class,
		VersioningModule.class
})
public class ChagolCoreModule {
}
