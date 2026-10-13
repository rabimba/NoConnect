package org.chagolchana.chagol.api.plugin.duplex;

import org.chagolchana.chagol.api.plugin.PluginFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a duplex transport.
 */
@NotNullByDefault
public interface DuplexPluginFactory extends PluginFactory<DuplexPlugin> {
}
