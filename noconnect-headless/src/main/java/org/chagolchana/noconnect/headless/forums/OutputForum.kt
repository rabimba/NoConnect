package org.chagolchana.noconnect.headless.forums

import org.chagolchana.noconnect.api.forum.Forum
import org.chagolchana.noconnect.headless.json.JsonDict

internal fun Forum.output() = JsonDict(
    "name" to name,
    "id" to id.bytes
)

internal fun Collection<Forum>.output() = map { it.output() }
