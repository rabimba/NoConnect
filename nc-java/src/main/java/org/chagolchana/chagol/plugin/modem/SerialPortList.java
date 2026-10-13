package org.chagolchana.chagol.plugin.modem;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SerialPortList {

	String[] getPortNames();
}
