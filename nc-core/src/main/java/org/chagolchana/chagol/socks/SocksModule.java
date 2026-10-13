package org.chagolchana.chagol.socks;

import org.chagolchana.chagol.api.plugin.TorSocksPort;
import org.chagolchana.socks.SocksSocketFactory;

import java.net.InetSocketAddress;

import javax.net.SocketFactory;

import dagger.Module;
import dagger.Provides;

import static org.chagolchana.chagol.api.plugin.TorConstants.CONNECT_TO_PROXY_TIMEOUT;
import static org.chagolchana.chagol.api.plugin.TorConstants.EXTRA_CONNECT_TIMEOUT;
import static org.chagolchana.chagol.api.plugin.TorConstants.EXTRA_SOCKET_TIMEOUT;

@Module
public class SocksModule {

	@Provides
	SocketFactory provideTorSocketFactory(@TorSocksPort int torSocksPort) {
		InetSocketAddress proxy = new InetSocketAddress("127.0.0.1",
				torSocksPort);
		return new SocksSocketFactory(proxy, CONNECT_TO_PROXY_TIMEOUT,
				EXTRA_CONNECT_TIMEOUT, EXTRA_SOCKET_TIMEOUT);
	}
}
