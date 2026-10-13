package org.chagolchana.noconnect.headless

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import org.chagolchana.chagol.account.AccountModule
import org.chagolchana.chagol.api.FeatureFlags
import org.chagolchana.chagol.api.db.DatabaseConfig
import org.chagolchana.chagol.api.mailbox.MailboxDirectory
import org.chagolchana.chagol.api.plugin.PluginConfig
import org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_CONTROL_PORT
import org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_SOCKS_PORT
import org.chagolchana.chagol.api.plugin.TorControlPort
import org.chagolchana.chagol.api.plugin.TorDirectory
import org.chagolchana.chagol.api.plugin.TorSocksPort
import org.chagolchana.chagol.api.plugin.TransportId
import org.chagolchana.chagol.api.plugin.duplex.DuplexPluginFactory
import org.chagolchana.chagol.api.plugin.simplex.SimplexPluginFactory
import org.chagolchana.chagol.battery.DefaultBatteryManagerModule
import org.chagolchana.chagol.event.DefaultEventExecutorModule
import org.chagolchana.chagol.plugin.tor.MacTorPluginFactory
import org.chagolchana.chagol.plugin.tor.UnixTorPluginFactory
import org.chagolchana.chagol.plugin.tor.WindowsTorPluginFactory
import org.chagolchana.chagol.system.ClockModule
import org.chagolchana.chagol.system.DefaultTaskSchedulerModule
import org.chagolchana.chagol.system.DefaultThreadFactoryModule
import org.chagolchana.chagol.system.DefaultWakefulIoExecutorModule
import org.chagolchana.chagol.system.DesktopSecureRandomModule
import org.chagolchana.chagol.util.OsUtils.isLinux
import org.chagolchana.chagol.util.OsUtils.isMac
import org.chagolchana.chagol.util.OsUtils.isWindows
import org.chagolchana.noconnect.headless.blogs.HeadlessBlogModule
import org.chagolchana.noconnect.headless.contact.HeadlessContactModule
import org.chagolchana.noconnect.headless.event.HeadlessEventModule
import org.chagolchana.noconnect.headless.forums.HeadlessForumModule
import org.chagolchana.noconnect.headless.messaging.HeadlessMessagingModule
import java.io.File
import java.util.Collections.emptyList
import javax.inject.Singleton

@Module(
    includes = [
        AccountModule::class,
        ClockModule::class,
        DefaultBatteryManagerModule::class,
        DefaultEventExecutorModule::class,
        DefaultTaskSchedulerModule::class,
        DefaultWakefulIoExecutorModule::class,
        DefaultThreadFactoryModule::class,
        DesktopSecureRandomModule::class,
        HeadlessBlogModule::class,
        HeadlessContactModule::class,
        HeadlessEventModule::class,
        HeadlessForumModule::class,
        HeadlessMessagingModule::class
    ]
)
internal class HeadlessModule(private val appDir: File) {

    @Provides
    @Singleton
    internal fun provideNoConnectService(noConnectService: NoConnectServiceImpl): NoConnectService = noConnectService

    @Provides
    @Singleton
    internal fun provideDatabaseConfig(): DatabaseConfig {
        val dbDir = File(appDir, "db")
        val keyDir = File(appDir, "key")
        return HeadlessDatabaseConfig(dbDir, keyDir)
    }

    @Provides
    @MailboxDirectory
    internal fun provideMailboxDirectory(): File {
        return File(appDir, "mailbox")
    }

    @Provides
    @TorDirectory
    internal fun provideTorDirectory(): File {
        return File(appDir, "tor")
    }

    @Provides
    @TorSocksPort
    internal fun provideTorSocksPort(): Int = DEFAULT_SOCKS_PORT

    @Provides
    @TorControlPort
    internal fun provideTorControlPort(): Int = DEFAULT_CONTROL_PORT

    @Provides
    @Singleton
    internal fun providePluginConfig(
        unixTor: UnixTorPluginFactory,
        macTor: MacTorPluginFactory,
        winTor: WindowsTorPluginFactory
    ): PluginConfig {
        val duplex: List<DuplexPluginFactory> = when {
            isLinux() -> listOf(unixTor)
            isMac() -> listOf(macTor)
            isWindows() -> listOf(winTor)
            else -> emptyList()
        }
        return object : PluginConfig {
            override fun getDuplexFactories(): Collection<DuplexPluginFactory> = duplex
            override fun getSimplexFactories(): Collection<SimplexPluginFactory> = emptyList()
            override fun shouldPoll(): Boolean = true
            override fun getTransportPreferences(): Map<TransportId, List<TransportId>> = emptyMap()
        }
    }

    @Provides
    @Singleton
    internal fun provideObjectMapper() = ObjectMapper()

    @Provides
    internal fun provideFeatureFlags() = object : FeatureFlags {
        override fun shouldEnableImageAttachments() = false
        override fun shouldEnableProfilePictures() = false
        override fun shouldEnableDisappearingMessages() = false
        override fun shouldEnablePrivateGroupsInCore() = false
        override fun shouldEnableForumsInCore() = true
        override fun shouldEnableBlogsInCore() = true
    }
}
