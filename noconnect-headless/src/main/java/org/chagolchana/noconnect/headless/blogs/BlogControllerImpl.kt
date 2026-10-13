package org.chagolchana.noconnect.headless.blogs

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.chagolchana.chagol.api.db.DbException
import org.chagolchana.chagol.api.db.TransactionManager
import org.chagolchana.chagol.api.identity.IdentityManager
import org.chagolchana.chagol.api.system.Clock
import org.chagolchana.chagol.util.StringUtils.utf8IsTooLong
import org.chagolchana.noconnect.api.blog.BlogConstants.MAX_BLOG_POST_TEXT_LENGTH
import org.chagolchana.noconnect.api.blog.BlogManager
import org.chagolchana.noconnect.api.blog.BlogPostFactory
import org.chagolchana.noconnect.api.blog.BlogPostHeader
import org.chagolchana.noconnect.headless.getFromJson
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
internal class BlogControllerImpl
@Inject
constructor(
    private val blogManager: BlogManager,
    private val blogPostFactory: BlogPostFactory,
    private val db: TransactionManager,
    private val identityManager: IdentityManager,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : BlogController {

    override fun listPosts(ctx: Context): Context {
        val posts = blogManager.blogs
            .flatMap { blog -> blogManager.getPostHeaders(blog.id) }
            .asSequence()
            .sortedBy { it.timeReceived }
            .map { header -> header.output(blogManager.getPostText(header.id)) }
            .toList()
        return ctx.json(posts)
    }

    override fun createPost(ctx: Context): Context {
        val text = ctx.getFromJson(objectMapper, "text")
        if (utf8IsTooLong(text, MAX_BLOG_POST_TEXT_LENGTH))
            throw BadRequestResponse("Blog post text is too long")

        val author = identityManager.localAuthor
        val blog = blogManager.getPersonalBlog(author)
        val now = clock.currentTimeMillis()
        val post = blogPostFactory.createBlogPost(blog.id, now, null, author, text)
        val header = db.transactionWithResult<BlogPostHeader, DbException>(true) { txn ->
            blogManager.addLocalPost(txn, post)
            return@transactionWithResult blogManager.getPostHeader(txn, blog.id, post.message.id)
        }
        return ctx.json(header.output(text))
    }

}
