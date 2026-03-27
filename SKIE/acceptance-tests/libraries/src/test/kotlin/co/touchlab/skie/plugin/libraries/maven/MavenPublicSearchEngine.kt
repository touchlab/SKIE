package co.touchlab.skie.plugin.libraries.maven

import co.touchlab.skie.plugin.libraries.library.Component
import co.touchlab.skie.plugin.libraries.maven.MavenPublicSearchEngine.Response.Library
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

// Faster but does not contain all libraries - those that were first released only recently (couple of months ago).
object MavenPublicSearchEngine : MavenSearchEngine() {

    private const val pageSize = 200

    // The Maven search is not reliable: it seems to ignore numbers and as such also returns iosarm32.
    // Plus it cannot contain the `-` and matches any substring instead of a suffix.
    private const val mavenSearchTerm = "iosarm"
    private const val fullSearchTerm = "-iosarm64"

    override suspend fun findComponents(fromPage: Int, numberOfPages: Int): List<Component> =
        getPages(fromPage, numberOfPages)
            .filter { it.id.endsWith(fullSearchTerm, ignoreCase = true) }
            .map { Component("${it.id}:${it.latestVersion}") }

    private suspend fun getPages(fromPage: Int, numberOfPages: Int): List<Library> =
        withHttpClient {
            PageDownloader<Library>(pageSize).getPages(fromPage, numberOfPages) { pageIndex ->
                val response = getPage(mavenSearchTerm, pageIndex.index)

                response.response.docs to PageDownloader.ItemCount.Items(response.response.numFound)
            }
        }

    private suspend fun HttpClient.getPage(searchTerm: String, page: Int): Response {
        val response = this.get("https://search.maven.org/solrsearch/select") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)

            parameter("q", searchTerm)
            parameter("wt", "json")
            parameter("rows", pageSize)
            parameter("start", pageSize * page)
        }

        if (!response.status.isSuccess()) {
            error("Maven central search failed: ${response.status} - ${response.bodyAsText()}")
        }

        return response.body<Response>()
    }

    @Serializable
    private data class Response(
        val response: ResponseBody,
    ) {

        @Serializable
        data class ResponseBody(
            val numFound: Int,
            val docs: List<Library>,
        )

        @Serializable
        data class Library(
            val id: String,
            val latestVersion: String,
        )
    }
}
