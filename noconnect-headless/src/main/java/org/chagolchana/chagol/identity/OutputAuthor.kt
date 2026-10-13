package org.chagolchana.chagol.identity

import org.chagolchana.chagol.api.identity.Author
import org.chagolchana.noconnect.api.identity.AuthorInfo
import org.chagolchana.noconnect.headless.json.JsonDict
import java.util.Locale

fun Author.output() = JsonDict(
    "formatVersion" to formatVersion,
    "id" to id.bytes,
    "name" to name,
    "publicKey" to publicKey.encoded
)

fun AuthorInfo.Status.output() = name.lowercase(Locale.US)
