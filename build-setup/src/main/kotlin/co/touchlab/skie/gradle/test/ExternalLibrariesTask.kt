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
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget

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

    @get:Input
    abstract val acceptableKotlinPrefixes: SetProperty<String>

    @get:Input
    abstract val platformSuffix: Property<String>

    @TaskAction
    fun downloadAndStoreLibraries() {
        val libraries = loadOrPopulateMavenCache()
        val librariesToTest = getAllLibrariesToTest(libraries)

        val versionedLibrariesToTest = getVersionedLibrariesToTest(librariesToTest)

        librariesToTestFile.get().asFile.writeText(
            versionedLibrariesToTest.map { it.dependencyString }.sorted().joinToString("\n")
        )
    }

    private fun getAllLibrariesToTest(libraries: List<Library>): Set<Library> {
        val platformSuffix = platformSuffix.get()
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

            configuration.resolvedConfiguration.lenientConfiguration.allModuleDependencies
        }.associateBy { it.module.id.module.toString().toLowerCase() }

        return allResolvedDependencies
            .filterKeys {
                it != "org.jetbrains.kotlin:kotlin-stdlib-common"
            }
            .filterKeys { key ->
                key.endsWith(platformSuffix) || !allResolvedDependencies.containsKey(key + platformSuffix)
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
            val configurationWithKotlin = createNativeConfiguration("library-test-configuration-single-$index-kotlin")
            val configuration = createNativeConfiguration("library-test-configuration-single-$index") {
                exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
            }

            project.dependencies {
                listOf(configurationWithKotlin, configuration).forEach {
                    it(
                        group = library.group,
                        name = library.name,
                        version = "+",
                    )
                }
            }

            val resolvedConfiguration = configuration.resolvedConfiguration
            resolvedConfiguration.errorIfExists?.let {
                logger.warn("Error resolving {}", library.moduleName, it)
                return@mapIndexedNotNull null
            }

            val unsupportedKotlinVersions = unsupportedKotlinVersions(configurationWithKotlin.resolvedConfiguration, library)
            if (unsupportedKotlinVersions.isNotEmpty()) {
                // TODO: Print info - which kotlin version is used
                logger.warn("Wrong kotlin version {} for {}", unsupportedKotlinVersions.joinToString(), library.moduleName)
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
            val downloadedLibraries = loadAll("*${platformSuffix.get()}").map {
                it.copy(name = it.name)
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

    private fun createNativeConfiguration(name: String, configure: Configuration.() -> Unit = { }): Configuration {
        return project.configurations.create(name) {
            isCanBeConsumed = false
            isCanBeResolved = true

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category::class.java, Category.LIBRARY))
                attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment::class.java, "non-jvm"))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                attribute(KotlinNativeTarget.konanTargetAttribute, KonanTarget.IOS_ARM64.name)
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "org.jetbrains.kotlin.klib")
            }

            configure()
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
            acceptableKotlinPrefixes.get().none { kotlinVersion.startsWith(it) }
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
            files
            null
        } catch (e: Throwable) {
            e
        }
}
