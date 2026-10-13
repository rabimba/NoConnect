package org.chagolchana.noconnect.headless.blogs

import io.javalin.http.Context

interface BlogController {

    fun listPosts(ctx: Context): Context

    fun createPost(ctx: Context): Context

}
