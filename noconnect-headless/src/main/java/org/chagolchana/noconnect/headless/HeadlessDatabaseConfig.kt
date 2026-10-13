package org.chagolchana.noconnect.headless

import org.chagolchana.chagol.api.crypto.KeyStrengthener
import org.chagolchana.chagol.api.db.DatabaseConfig
import java.io.File

internal class HeadlessDatabaseConfig(private val dbDir: File, private val keyDir: File) :
    DatabaseConfig {

    override fun getDatabaseDirectory() = dbDir

    override fun getDatabaseKeyDirectory() = keyDir

    override fun getKeyStrengthener(): KeyStrengthener? = null
}
