package co.touchlab.skie.plugin.libraries.maven

import co.touchlab.skie.plugin.libraries.library.Component
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

object MavenInternalSearchEngine : MavenSearchEngine() {

    private const val pageSize = 20

    private const val mavenSearchTerm = "-iosarm64"

    override suspend fun findComponents(fromPage: Int, numberOfPages: Int): List<Component> =
        getPages(fromPage, numberOfPages)
            .map { Component("${it.namespace}:${it.name}:+") }

    private suspend fun getPages(fromPage: Int, numberOfPages: Int): List<Response.Component> =
        withHttpClient {
            PageDownloader<Response.Component>(pageSize).getPages(fromPage, numberOfPages) { pageIndex ->
                val response = getPage(pageIndex.index)

                response.components to PageDownloader.ItemCount.Pages(response.pageCount)
            }
        }

    private suspend fun HttpClient.getPage(page: Int): Response {
        val response = this.post("https://central.sonatype.com/api/internal/browse/components") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {"size": $pageSize, "page": $page, "searchTerm": "$mavenSearchTerm", "filter": []}
                """.trimIndent(),
            )
        }

        if (!response.status.isSuccess()) {
            error("Maven central search failed: ${response.status} - ${response.bodyAsText()}")
        }

        return response.body<Response>()
    }

    @Serializable
    private data class Response(
        val pageCount: Int,
        val components: List<Component>,
    ) {

        @Serializable
        data class Component(
            val namespace: String,
            val name: String,
        )
    }
}
