package org.chagolchana.noconnect.headless

import dagger.Component
import org.chagolchana.chagol.ChagolCoreEagerSingletons
import org.chagolchana.chagol.ChagolCoreModule
import org.chagolchana.chagol.ChagolJavaEagerSingletons
import org.chagolchana.chagol.ChagolJavaModule
import org.chagolchana.chagol.api.crypto.CryptoComponent
import org.chagolchana.noconnect.NoConnectCoreEagerSingletons
import org.chagolchana.noconnect.NoConnectCoreModule
import org.chagolchana.noconnect.api.test.TestDataCreator
import javax.inject.Singleton

@Component(
    modules = [
        ChagolCoreModule::class,
        ChagolJavaModule::class,
        NoConnectCoreModule::class,
        HeadlessTestModule::class
    ]
)
@Singleton
internal interface NoConnectHeadlessTestApp : ChagolCoreEagerSingletons, NoConnectCoreEagerSingletons,
    ChagolJavaEagerSingletons, HeadlessEagerSingletons {

    fun getRouter(): Router

    fun getCryptoComponent(): CryptoComponent

    fun getTestDataCreator(): TestDataCreator
}
