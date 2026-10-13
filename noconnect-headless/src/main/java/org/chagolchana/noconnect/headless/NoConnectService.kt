package org.chagolchana.noconnect.headless

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.output.TermUi.prompt
import org.chagolchana.chagol.api.account.AccountManager
import org.chagolchana.chagol.api.crypto.DecryptionException
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator.QUITE_WEAK
import org.chagolchana.chagol.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH
import org.chagolchana.chagol.api.lifecycle.LifecycleManager
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

interface NoConnectService {
    fun start()
    fun stop()
}

@Immutable
@Singleton
internal class NoConnectServiceImpl
@Inject
constructor(
    private val accountManager: AccountManager,
    private val lifecycleManager: LifecycleManager,
    private val passwordStrengthEstimator: PasswordStrengthEstimator
) : NoConnectService {

    override fun start() {
        if (!accountManager.accountExists()) {
            createAccount()
        } else {
            val password = prompt("Password", hideInput = true)
                ?: throw UsageError("Could not get password. Is STDIN connected?")
            try {
                accountManager.signIn(password)
            } catch (e: DecryptionException) {
                echo("Error: Password invalid")
                exitProcess(1)
            }
        }
        val dbKey = accountManager.databaseKey ?: throw AssertionError()
        lifecycleManager.startServices(dbKey)
        lifecycleManager.waitForStartup()
    }

    override fun stop() {
        lifecycleManager.stopServices()
        lifecycleManager.waitForShutdown()
    }

    private fun createAccount() {
        echo("No account found. Let's create one!\n\n")
        val nickname = prompt("Nickname") { nickname ->
            if (nickname.length > MAX_AUTHOR_NAME_LENGTH)
                throw UsageError("Please choose a shorter nickname!")
            nickname
        }
        val password =
            prompt("Password", hideInput = true, requireConfirmation = true) { password ->
                if (passwordStrengthEstimator.estimateStrength(password) < QUITE_WEAK)
                    throw UsageError("Please enter a stronger password!")
                password
            }
        if (nickname == null || password == null)
            throw UsageError("Could not get account information. Is STDIN connected?")
        accountManager.createAccount(nickname, password)
    }

}
