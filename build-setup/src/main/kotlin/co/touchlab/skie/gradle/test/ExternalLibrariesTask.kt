package co.touchlab.skie.gradle.test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.attributes.Usage
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.removeSuffixIfPresent

data class VersionedLibrary(
    val library: Library,
    val version: String,
) {
    constructor(
        group: String,
        name: String,
        version: String,
    ): this(
        library = Library(group, name),
        version = version,
    )

    val dependencyString: String
        get() = "${library.moduleName}:$version"
}

data class Library(val group: String, val name: String) {
    val moduleName: String
        get() = "$group:$name"
}

abstract class ExternalLibrariesTask: DefaultTask() {

    @get:OutputFile
    abstract val mavenSearchCache: RegularFileProperty

    @get:OutputFile
    abstract val librariesToTestFile: RegularFileProperty

    @TaskAction
    fun downloadAndStoreLibraries() {
        val libraries = loadOrPopulateMavenCache()
        val librariesToTest = getAllLibrariesToTest(libraries)

        val versionedLibrariesToTest = getVersionedLibrariesToTest(librariesToTest)

        val json = versionedLibrariesToTest.associate { library ->
            library.dependencyString to listOf(library.dependencyString)
        }

        librariesToTestFile.get().asFile.writeText(
            JsonOutput.toJson(json),
        )
    }

    private fun getAllLibrariesToTest(libraries: List<Library>): Set<Library> {
        val allResolvedDependencies = libraries.flatMapIndexed { index, library ->
            logger.info("Resolving {}", library.moduleName)
            val configuration = createNativeConfiguration("library-test-configuration-$index")

            project.dependencies {
                configuration(
                    group = library.group,
                    name = library.name,
                    version = "+",
                )
            }

            val resolvedConfiguration = configuration.resolvedConfiguration
            resolvedConfiguration.errorIfExists?.let {
                logger.warn("Error resolving {}", library.moduleName, it)
                return@flatMapIndexed emptyList()
            }

            val unsupportedKotlinVersions = unsupportedKotlinVersions(resolvedConfiguration, library)
            if (unsupportedKotlinVersions.isNotEmpty()) {
                // TODO: Print info - which kotlin version is used
                logger.warn("Wrong kotlin version {} for {}", unsupportedKotlinVersions.joinToString(), library.moduleName)
                return@flatMapIndexed emptyList()
            }

            fun Set<ResolvedDependency>.flatten(): Set<ResolvedDependency> = flatMap { listOf(it) + it.children.flatten() }.toSet()

            resolvedConfiguration.firstLevelModuleDependencies.flatten()
        }.associateBy { it.module.id.module.toString().toLowerCase() }

        return allResolvedDependencies
            .filterKeys { key ->
                !key.endsWith(platformSuffix) || !allResolvedDependencies.containsKey(key.removeSuffixIfPresent(platformSuffix))
            }
            .values
            .map {
                Library(
                    group = it.moduleGroup,
                    name = it.moduleName,
                )
            }
            .toSet()
    }

    private fun getVersionedLibrariesToTest(librariesToTest: Set<Library>) =
        librariesToTest.mapIndexedNotNull { index, library ->
            val configuration = createNativeConfiguration("library-test-configuration-single-$index")

            project.dependencies {
                configuration(
                    group = library.group,
                    name = library.name,
                    version = "+",
                )
            }

            val resolvedConfiguration = configuration.resolvedConfiguration
            resolvedConfiguration.errorIfExists?.let {
                logger.warn("Error resolving {}", library.moduleName, it)
                return@mapIndexedNotNull null
            }

            resolvedConfiguration.firstLevelModuleDependencies.single().let {
                VersionedLibrary(
                    group = it.moduleGroup,
                    name = it.moduleName,
                    version = it.moduleVersion,
                )
            }
        }

    private fun loadOrPopulateMavenCache(): List<Library> {
        val mavenSearchCacheFile = mavenSearchCache.get().asFile
        return if (mavenSearchCacheFile.exists()) {
            @Suppress("UNCHECKED_CAST")
            val json = JsonSlurper().parse(mavenSearchCacheFile) as List<Map<String, String>>
            json.map {
                Library(
                    group = it.getValue("group"),
                    name = it.getValue("name"),
                )
            }
        } else {
            val downloadedLibraries = loadAll("*$platformSuffix").map {
                it.copy(name = it.name.removeSuffix(platformSuffix).removeSuffix("-iosArm64"))
            }

            val librariesJson = JsonOutput.toJson(
                downloadedLibraries.map {
                    mapOf(
                        "group" to it.group,
                        "name" to it.name,
                    )
                }
            )

            mavenSearchCacheFile.parentFile.mkdirs()
            mavenSearchCacheFile.writeText(librariesJson)

            downloadedLibraries
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadAll(query: String, fromPage: Int = 0): List<Library> {
        val client = HttpClient(Java)
        val result = runBlocking {
            // TODO: Print errors if they happen
            client.post("https://central.sonatype.com/v1/browse") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody("""
                    {"size": 100, "page": $fromPage, "searchTerm": "$query", "filter": []}
                """.trimIndent())
            }.bodyAsText()
        }

        val json = JsonSlurper().parseText(result) as Map<String, Any>
        val pageCount = json["pageCount"] as Int
        val components = json["components"] as List<Map<String, Any>>

        val libraries = components.map { component ->
            Library(
                group = component["namespace"] as String,
                name = component["name"] as String,
            )
        }

        return if (fromPage < pageCount) {
            libraries + loadAll(query, fromPage + 1)
        } else {
            libraries
        }
    }

    private fun createNativeConfiguration(name: String): Configuration {
        return project.configurations.create(name) {
            isCanBeConsumed = false
            isCanBeResolved = true

            attributes.attribute(
                KotlinPlatformType.attribute,
                KotlinPlatformType.native,
            )
            attributes.attribute(KotlinNativeTarget.konanTargetAttribute, KonanTarget.IOS_ARM64.name)
            attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
        }
    }

    private fun unsupportedKotlinVersions(configuration: ResolvedConfiguration, library: Library): Set<String> {
        val kotlinVersions = findAllKotlinVersions(configuration)
        if (kotlinVersions.size > 1) {
            logger.warn("Multiple Kotlin versions {} ({})", library.moduleName, kotlinVersions.joinToString())
        }
        if (kotlinVersions.isEmpty()) {
            logger.warn("No Kotlin version found for {}", library.moduleName)
        }
        return kotlinVersions.filter { kotlinVersion ->
            allowedKotlinPrefixes.none { kotlinVersion.startsWith(it) }
        }.toSet()
    }

    private fun findAllKotlinVersions(configuration: ResolvedConfiguration): Set<String> {
        return configuration.firstLevelModuleDependencies.flatMap { findAllKotlinVersions(it) }.toSet()
    }

    private fun findAllKotlinVersions(dependency: ResolvedDependency): Set<String> {
        val thisDependency = if (dependency.moduleGroup == "org.jetbrains.kotlin" && dependency.moduleName == "kotlin-stdlib-common") {
            setOf(dependency.moduleVersion)
        } else {
            emptySet()
        }
        return thisDependency + dependency.children.flatMap { findAllKotlinVersions(it) }.toSet()
    }

    private val ResolvedConfiguration.errorIfExists: Throwable?
        get() = try {
            rethrowFailure()
            null
        } catch (e: Exception) {
            e
        }

    companion object {
        private val allowedKotlinPrefixes = setOf("1.7.", "1.6.", "1.5.", "1.4.")
        private val platformSuffix = "-iosarm64"
    }
}
