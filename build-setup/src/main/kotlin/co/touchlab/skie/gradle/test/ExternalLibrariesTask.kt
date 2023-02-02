package co.touchlab.skie.gradle.test

import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.net.URL


data class Library(val group: String, val name: String, val version: String) {
    val moduleName: String
        get() = "$group:$name"

    val dependencyString: String
        get() = "$moduleName:$version"
}

abstract class ExternalLibrariesTask: DefaultTask() {

    @get:OutputDirectory
    abstract val externalLibrariesDir: DirectoryProperty

    @get:Internal
    val mavenSearchCache: Provider<RegularFile> = externalLibrariesDir.file("maven-search-cache.json")

    @get:Internal
    val librariesToTestFile: Provider<RegularFile> = externalLibrariesDir.file("libraries-to-test.json")

    @TaskAction
    fun downloadAndStoreLibraries() {

        val mavenSearchCacheFile = mavenSearchCache.get().asFile
        val libraries = if (mavenSearchCacheFile.exists()) {
            val json = JsonSlurper().parse(mavenSearchCacheFile) as List<Map<String, String>>
            json.map {
                Library(
                    group = it.getValue("group"),
                    name = it.getValue("name"),
                    version = it.getValue("version"),
                )
            }
        } else {
            val downloadedLibraries = loadAll("*-iosarm64").map {
                it.copy(name = it.name.removeSuffix("-iosarm64").removeSuffix("-iosArm64"))
            }

            val librariesJson = JsonOutput.toJson(
                downloadedLibraries.map {
                    mapOf(
                        "group" to it.group,
                        "name" to it.name,
                        "version" to it.version,
                    )
                }
            )

            mavenSearchCacheFile.parentFile.mkdirs()
            mavenSearchCacheFile.writeText(librariesJson)

            downloadedLibraries
        }

        fun findAllKotlinVersions(dependency: ResolvedDependency): Set<String> {
            val thisDependency = if (dependency.moduleGroup == "org.jetbrains.kotlin" && dependency.moduleName == "kotlin-stdlib-common") {
                setOf(dependency.moduleVersion)
            } else {
                emptySet()
            }
            return thisDependency + dependency.children.flatMap { findAllKotlinVersions(it) }.toSet()
        }

        val allowedKotlinPrefixes = setOf("1.7.", "1.6.", "1.5.", "1.4.")
        fun dependsOnCorrectKotlinVersion(dependency: ResolvedDependency): Boolean {
            val kotlinVersions = findAllKotlinVersions(dependency)
            if (kotlinVersions.size > 1) {
                println("Multiple Kotlin versions: ${dependency.name} - ${kotlinVersions.joinToString()}")
            }
            return kotlinVersions.all { kotlinVersion ->
                allowedKotlinPrefixes.any { kotlinVersion.startsWith(it) }
            }
        }

        val moduleUsages = mutableMapOf<String, Int>()
        val resolvedLibraries = libraries.mapIndexedNotNull { index, library ->
            println("Resolving ${library.moduleName}")
            val configuration = project.configurations.create("library-test-configuration-$index") {
                isCanBeConsumed = false
                isCanBeResolved = true

                attributes.attribute(
                    KotlinPlatformType.attribute,
                    KotlinPlatformType.native,
                )
                attributes.attribute(KotlinNativeTarget.konanTargetAttribute, KonanTarget.IOS_ARM64.name)
                attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
            }

            project.dependencies {
                configuration("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                    version {
                        strictly("1.6.4")
                    }
                }
                configuration("org.jetbrains.kotlinx:kotlinx-datetime") {
                    version {
                        strictly("0.4.0")
                    }
                }
                configuration(
                    group = library.group,
                    name = library.name,
                    version = "+",
                )
            }

            val resolvedConfiguration = configuration.resolvedConfiguration
            if (resolvedConfiguration.hasError()) {
                return@mapIndexedNotNull null
            }

            if (resolvedConfiguration.firstLevelModuleDependencies.any {
                    !dependsOnCorrectKotlinVersion(it)
                }) {
                return@mapIndexedNotNull null
            }

            val realLibrary = resolvedConfiguration.firstLevelModuleDependencies.first {
                it.module.id.module.toString().equals(library.moduleName, ignoreCase = true)
            }.let {
                Library(
                    group = it.moduleGroup,
                    name = it.moduleName,
                    version = it.moduleVersion,
                )
            }

            resolvedConfiguration.lenientConfiguration.allModuleDependencies.forEach {
                val moduleName = it.module.id.module.toString()
                val currentUsages = moduleUsages.getOrDefault(moduleName, 0)
                moduleUsages[moduleName] = currentUsages + it.parents.filter { it.moduleGroup != "co.touchlab.skie" }.size
            }

            val dependencies = configuration.incoming.resolutionResult.allComponents.map { it.toString() }
            val exportedLibraries = dependencies.filterNot { it.startsWith("project :") }

            realLibrary to exportedLibraries
        }

        val librariesToTest = resolvedLibraries.filter {
            moduleUsages[it.first.moduleName] == 0
        }

        val json = librariesToTest.associate { (library, exportedLibraries) ->
            library.dependencyString to exportedLibraries
        }

        librariesToTestFile.get().asFile.writeText(
            JsonOutput.toJson(json),
        )
    }

    private fun loadAll(query: String, fromPage: Int = 0): List<Library> {
        val result =
            URL("https://central.sonatype.com/_next/data/B6q14ZLvnYjRKqRX4jJ3i/search.json?q=${query}&page=${fromPage}").readText()

        val json = JsonSlurper().parseText(result) as Map<String, Any>
        val pageProperties = json["pageProps"] as Map<String, Any>
        val componentsList = pageProperties["componentsList"] as Map<String, Any>
        val pageCount = componentsList["pageCount"] as Int
        val components = componentsList["components"] as List<Map<String, Any>>

        val libraries = components.mapNotNull { component ->
            if (component["latestVersion"] as Boolean) {
                Library(
                    group = component["namespace"] as String,
                    name = component["name"] as String,
                    version = component["version"] as String,
                )
            } else {
                null
            }
        }

        return if (fromPage < pageCount) {
            libraries + loadAll(query, fromPage + 1)
        } else {
            libraries
        }
    }
}
