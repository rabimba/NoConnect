package org.chagolchana.noconnect.headless

import dagger.Component
import org.chagolchana.chagol.ChagolCoreEagerSingletons
import org.chagolchana.chagol.ChagolCoreModule
import org.chagolchana.chagol.ChagolJavaEagerSingletons
import org.chagolchana.chagol.ChagolJavaModule
import org.chagolchana.noconnect.NoConnectCoreEagerSingletons
import org.chagolchana.noconnect.NoConnectCoreModule
import java.security.SecureRandom
import javax.inject.Singleton

@Component(
    modules = [
        ChagolCoreModule::class,
        ChagolJavaModule::class,
        NoConnectCoreModule::class,
        HeadlessModule::class
    ]
)
@Singleton
internal interface NoConnectHeadlessApp : ChagolCoreEagerSingletons, NoConnectCoreEagerSingletons,
    ChagolJavaEagerSingletons, HeadlessEagerSingletons {

    fun getRouter(): Router

    fun getSecureRandom(): SecureRandom
}
