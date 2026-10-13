package org.chagolchana.chagol;

import org.chagolchana.chagol.io.DnsModule;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.network.JavaNetworkModule;
import org.chagolchana.chagol.plugin.tor.CircumventionModule;
import org.chagolchana.chagol.socks.SocksModule;
import org.chagolchana.chagol.system.JavaSystemModule;

import dagger.Module;

@Module(includes = {
		CircumventionModule.class,
		DnsModule.class,
		JavaNetworkModule.class,
		JavaSystemModule.class,
		ModularMailboxModule.class,
		SocksModule.class
})
public class ChagolJavaModule {

}
