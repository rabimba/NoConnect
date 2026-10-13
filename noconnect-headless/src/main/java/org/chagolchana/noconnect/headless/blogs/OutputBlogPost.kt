package org.chagolchana.noconnect.headless.blogs

import org.chagolchana.chagol.identity.output
import org.chagolchana.noconnect.api.blog.BlogPostHeader
import org.chagolchana.noconnect.api.blog.MessageType
import org.chagolchana.noconnect.headless.json.JsonDict
import java.util.Locale

internal fun BlogPostHeader.output(text: String) = JsonDict(
    "text" to text,
    "author" to author.output(),
    "authorStatus" to authorInfo.status.output(),
    "type" to type.output(),
    "id" to id.bytes,
    "parentId" to parentId?.bytes,
    "read" to isRead,
    "rssFeed" to isRssFeed,
    "timestamp" to timestamp,
    "timestampReceived" to timeReceived
)

internal fun MessageType.output() = name.lowercase(Locale.US)
