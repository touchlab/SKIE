package co.touchlab.skie.plugin.libraries.maven

import co.touchlab.skie.plugin.libraries.library.Component
import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

abstract class MavenSearchEngine {

    fun findIosArm64Libraries(fromPage: Int, numberOfPages: Int): List<TestedLibrary> = runBlocking {
        findComponents(fromPage, numberOfPages)
            .distinct()
            .sortedBy { it.coordinate }
            .mapIndexed { index, component ->
                TestedLibrary(index, component, emptyList())
            }
    }

    protected inline fun <T> withHttpClient(action: HttpClient.() -> T): T =
        HttpClient(Java) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }.use(action)

    protected abstract suspend fun findComponents(fromPage: Int, numberOfPages: Int): List<Component>
}
