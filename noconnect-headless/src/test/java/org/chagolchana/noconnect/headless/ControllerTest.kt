package org.chagolchana.noconnect.headless

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.Context
import io.javalin.http.util.ContextUtil
import io.mockk.mockk
import org.chagolchana.chagol.api.connection.ConnectionRegistry
import org.chagolchana.chagol.api.contact.Contact
import org.chagolchana.chagol.api.contact.ContactManager
import org.chagolchana.chagol.api.db.TransactionManager
import org.chagolchana.chagol.api.identity.Author
import org.chagolchana.chagol.api.identity.IdentityManager
import org.chagolchana.chagol.api.identity.LocalAuthor
import org.chagolchana.chagol.api.sync.Group
import org.chagolchana.chagol.api.sync.Message
import org.chagolchana.chagol.api.system.Clock
import org.chagolchana.chagol.test.TestUtils.getAuthor
import org.chagolchana.chagol.test.TestUtils.getClientId
import org.chagolchana.chagol.test.TestUtils.getContact
import org.chagolchana.chagol.test.TestUtils.getGroup
import org.chagolchana.chagol.test.TestUtils.getLocalAuthor
import org.chagolchana.chagol.test.TestUtils.getMessage
import org.chagolchana.chagol.util.StringUtils.getRandomString
import org.chagolchana.noconnect.api.conversation.ConversationManager
import org.chagolchana.noconnect.headless.event.WebSocketController
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class ControllerTest {

    protected val db = mockk<TransactionManager>()
    protected val contactManager = mockk<ContactManager>()
    protected val conversationManager = mockk<ConversationManager>()
    protected val identityManager = mockk<IdentityManager>()
    protected val connectionRegistry = mockk<ConnectionRegistry>()
    protected val clock = mockk<Clock>()
    protected val ctx = mockk<Context>()

    protected val webSocketController = mockk<WebSocketController>()

    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val outputCtx = ContextUtil.init(request, response)

    protected val objectMapper = ObjectMapper()

    protected val group: Group = getGroup(getClientId(), 0)
    protected val author: Author = getAuthor()
    protected val localAuthor: LocalAuthor = getLocalAuthor()
    protected val contact: Contact = getContact(author, localAuthor.id, true)
    protected val message: Message = getMessage(group.id)
    protected val text: String = getRandomString(5)
    protected val timestamp = 42L
    protected val unreadCount = 42

    protected fun assertJsonEquals(json: String, obj: Any) {
        assertEquals(json, outputCtx.json(obj).resultString(), STRICT)
    }

}
