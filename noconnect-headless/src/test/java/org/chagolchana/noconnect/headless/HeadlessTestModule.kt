package org.chagolchana.noconnect.headless

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import org.chagolchana.chagol.account.AccountModule
import org.chagolchana.chagol.api.db.DatabaseConfig
import org.chagolchana.chagol.api.mailbox.MailboxDirectory
import org.chagolchana.chagol.api.plugin.PluginConfig
import org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_CONTROL_PORT
import org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_SOCKS_PORT
import org.chagolchana.chagol.api.plugin.TorControlPort
import org.chagolchana.chagol.api.plugin.TorSocksPort
import org.chagolchana.chagol.api.plugin.TransportId
import org.chagolchana.chagol.api.plugin.duplex.DuplexPluginFactory
import org.chagolchana.chagol.api.plugin.simplex.SimplexPluginFactory
import org.chagolchana.chagol.event.DefaultEventExecutorModule
import org.chagolchana.chagol.system.ClockModule
import org.chagolchana.chagol.system.DefaultTaskSchedulerModule
import org.chagolchana.chagol.system.DefaultThreadFactoryModule
import org.chagolchana.chagol.system.DefaultWakefulIoExecutorModule
import org.chagolchana.chagol.test.TestFeatureFlagModule
import org.chagolchana.chagol.test.TestSecureRandomModule
import org.chagolchana.noconnect.api.test.TestAvatarCreator
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
        DefaultEventExecutorModule::class,
        DefaultTaskSchedulerModule::class,
        DefaultWakefulIoExecutorModule::class,
        DefaultThreadFactoryModule::class,
        TestFeatureFlagModule::class,
        TestSecureRandomModule::class,
        HeadlessBlogModule::class,
        HeadlessContactModule::class,
        HeadlessEventModule::class,
        HeadlessForumModule::class,
        HeadlessMessagingModule::class
    ]
)
internal class HeadlessTestModule(private val appDir: File) {

    @Provides
    @Singleton
    internal fun provideNoConnectService(noConnectService: NoConnectTestServiceImpl): NoConnectService =
        noConnectService

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
    @TorSocksPort
    internal fun provideTorSocksPort(): Int = DEFAULT_SOCKS_PORT

    @Provides
    @TorControlPort
    internal fun provideTorControlPort(): Int = DEFAULT_CONTROL_PORT

    @Provides
    @Singleton
    internal fun providePluginConfig(): PluginConfig {
        return object : PluginConfig {
            override fun getDuplexFactories(): Collection<DuplexPluginFactory> = emptyList()
            override fun getSimplexFactories(): Collection<SimplexPluginFactory> = emptyList()
            override fun shouldPoll(): Boolean = false
            override fun getTransportPreferences(): Map<TransportId, List<TransportId>> = emptyMap()
        }
    }

    @Provides
    @Singleton
    internal fun provideObjectMapper() = ObjectMapper()

    @Provides
    internal fun provideTestAvatarCreator() = TestAvatarCreator { null }
}
