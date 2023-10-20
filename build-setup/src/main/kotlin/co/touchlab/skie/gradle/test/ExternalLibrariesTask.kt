package co.touchlab.skie.gradle.test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.exclude
import org.gradle.tooling.BuildException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File

data class VersionedLibrary(
    val library: Library,
    val version: String,
) {

    constructor(
        group: String,
        name: String,
        version: String,
    ) : this(
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

abstract class ExternalLibrariesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val resolutionTempDir: DirectoryProperty

    @get:OutputFile
    abstract val mavenSearchCache: RegularFileProperty

    @get:OutputFile
    abstract val librariesToTestFile: RegularFileProperty

    @get:Input
    abstract val acceptableKotlinPrefixes: SetProperty<String>

    @get:Input
    abstract val kotlinVersion: Property<String>

    @get:Input
    abstract val target: Property<KonanTarget>

    @TaskAction
    fun downloadAndStoreLibraries() {
        val libraries = loadOrPopulateMavenCache()

        val librariesToTest = getAllLibrariesToTest(libraries)

        val versionedLibrariesToTest = getVersionedLibrariesToTest(librariesToTest)

        librariesToTestFile.get().asFile.writeText(
            versionedLibrariesToTest.sorted().joinToString("\n"),
        )
    }

    private fun getAllLibrariesToTest(libraries: List<Library>): Set<String> {
        val resolutionProjectDir = resolutionTempDir.dir("resolve-all-libraries").get().asFile.also { it.mkdirs() }
        resolutionProjectDir.resolve("input").writeText(
            libraries.joinToString("\n") { "${it.moduleName}:+" },
        )

        runSubGradle(
            projectDir = resolutionProjectDir,
            imports = listOf(
                "org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget",
            ),
            tasks = listOf("findTestableLibraries"),
            code = """
            val libraries = project.file("input").readLines()
            kotlin {
                libraries.withIndex().forEach { (index, library) ->
                    ${target.get().presetName}("library_${'$'}index") {
                        attributes {
                            attribute(Attribute.of("co.touchlab.skie.test.library", String::class.java), "library_${'$'}index")
                        }
                        // This configuration is created by Kotlin, but doesn't copy our attributes, so we need to do it manually
                        project.configurations.named(this.name + "CInteropApiElements").configure {
                            attributes {
                                attribute(Attribute.of("co.touchlab.skie.test.library", String::class.java), "library_${'$'}index")
                            }
                        }
                    }
                    sourceSets["library_${'$'}{index}Main"].dependencies {
                        implementation(library)
                    }
                }
            }
            tasks.register("findTestableLibraries") {
                doLast {
                    val allModules = kotlin.targets
                        .filter { it !is KotlinMetadataTarget }
                        .flatMap { target ->
                            val resolvedConfiguration = configurations.getByName(target.name + "CompileKlibraries").resolvedConfiguration
                            resolvedConfiguration.lenientConfiguration.allModuleDependencies
                        }
                        .map { it.module.id.module.toString().lowercase() }
                        .filterNot { it.startsWith("org.jetbrains.kotlin:kotlin-stdlib", ignoreCase = true) }
                        .filter { it.endsWith("-${target.get().presetName}", ignoreCase = true) }
                        .toSet()
                        .sorted()
                    project.file("output").writeText(allModules.joinToString("\n"))
                }
            }
            """.trimIndent(),
        )

        return resolutionProjectDir.resolve("output").readLines().toSet()
    }

    private fun getVersionedLibrariesToTest(librariesToTest: Collection<String>): Set<String> {
        val resolutionProjectDir = resolutionTempDir.dir("resolve-latest-versions").get().asFile.also { it.mkdirs() }
        resolutionProjectDir.resolve("input").writeText(
            librariesToTest.joinToString("\n") { "$it:+" },
        )
        runSubGradle(
            projectDir = resolutionProjectDir,
            imports = listOf(
                "org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget",
                "org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask",
                "org.jetbrains.kotlin.gradle.plugin.mpp.Framework",
            ),
            tasks = listOf(
                "compileAllNativeTargets",
                "resolveLatestVersions",
            ),
            code = """
            val libraries = project.file("input").readLines()
            kotlin {
                libraries.withIndex().forEach { (index, library) ->
                    ${target.get().presetName}("library_${'$'}index") {
                        attributes {
                            attribute(Attribute.of("co.touchlab.skie.test.library", String::class.java), "library_${'$'}index")
                        }
                        binaries {
                            framework(namePrefix = "library_${'$'}index", buildTypes = listOf(DEBUG)) {
                                isStatic = true
                                export(library)
                            }
                        }
                        // This configuration is created by Kotlin, but doesn't copy our attributes, so we need to do it manually
                        project.configurations.named(this.name + "CInteropApiElements").configure {
                            attributes {
                                attribute(Attribute.of("co.touchlab.skie.test.library", String::class.java), "library_${'$'}index")
                            }
                        }
                        project.configurations.matching { it.name == this.name + "DebugFrameworkIosFat" }.configureEach {
                            attributes {
                                attribute(Attribute.of("co.touchlab.skie.test.library", String::class.java), "library_${'$'}index")
                                attribute(Attribute.of("co.touchlab.skie.test.disambiguate", String::class.java), "fat framework")
                            }
                        }
                    }
                    sourceSets["library_${'$'}{index}Main"].dependencies {
                        api(library)
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                            version {
                                strictly("[1.6.4,)")
                            }
                        }
                    }
                }
            }
            val nativeTargets = kotlin.targets.filterIsInstance<KotlinNativeTarget>()
            val compileAllNativeTargets by tasks.registering {
                dependsOn(
                    nativeTargets.flatMap {
                        it.binaries.withType<Framework>().map { it.linkTaskProvider }
                    }
                )
            }
            val resolveLatestVersions by tasks.registering {
                mustRunAfter(compileAllNativeTargets)
                doLast {
                    val unresolvedLibraries = mutableListOf<String>()
                    val allModules = nativeTargets.flatMap { target ->
                            try {
                                val compileTaskState = target.compilations.getByName("main").compileTaskProvider.get().state
                                val linkTaskState = target.binaries.withType<Framework>().single().linkTask.state
                                val resolvedConfiguration = configurations.getByName(target.name + "CompileKlibraries").resolvedConfiguration
                                compileTaskState.rethrowFailure()
                                linkTaskState.rethrowFailure()
                                resolvedConfiguration.firstLevelModuleDependencies
                            } catch (e: Throwable) {
                                logger.warn("Error resolving dependencies for ${'$'}{target.name}", e)
                                unresolvedLibraries.add(libraries[target.name.split("_")[1].toInt()])
                                emptyList()
                            }
                        }
                        .map { it.module.id.toString() }
                        .filterNot { it.startsWith("org.jetbrains.kotlin:kotlin-stdlib", ignoreCase = true) }
                        .toSet()
                        .sorted()
                    project.file("output").writeText(allModules.joinToString("\n"))
                    project.file("unresolved").writeText(unresolvedLibraries.joinToString("\n"))
                }
            }
            """.trimIndent(),
        )

        return resolutionProjectDir.resolve("output").readLines().toSet()
    }

    private fun runSubGradle(
        projectDir: File,
        imports: List<String> = emptyList(),
        tasks: List<String> = emptyList(),
        @Language("kotlin") code: String,
    ) {
        projectDir.resolve("build.gradle.kts").writeText(
            imports.joinToString("\n") { "import $it" } + """
            |
            |plugins {
            |    kotlin("multiplatform") version "${kotlinVersion.get()}"
            |}
            |
            |repositories {
            |    mavenCentral()
            |    google()
            |    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
            |    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
            |}
        """.trimMargin() + "\n\n" + code,
        )
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("gradle.properties").writeText(
            """
            org.gradle.jvmargs=-Xmx8g -XX:+UseParallelGC
            org.gradle.parallel=true
            kotlin.native.cacheKind=none
        """.trimIndent(),
        )
        projectDir.resolve("src/commonMain/kotlin/Empty.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package co.touchlab.skie.test
            """.trimIndent(),
            )
        }

        GradleConnector.newConnector()
            .forProjectDirectory(projectDir)
            .connect().use { connection ->
                //obtain some information from the build
                val environment = connection.model(BuildEnvironment::class.java).get()

                try {
                    //run some tasks
                    val logFile = projectDir.resolve("run.log").outputStream()
                    connection.newBuild()
                        .forTasks(*tasks.toTypedArray())
                        .withArguments("--continue")
                        .setStandardOutput(logFile)
                        .setStandardError(logFile)
                        .run()
                } catch (e: BuildException) {
                    // We're ignoring the build error, as we expect some tasks to fail and that's okay.
                }
            }
    }

    private fun getVersionedLibrariesToTest(librariesToTest: Set<Library>) =
        librariesToTest.mapIndexedNotNull { index, library ->
            val dependency = project.dependencies.create(
                group = library.group,
                name = library.name,
                version = "+",
            )
            val configurationWithKotlin = createNativeConfiguration(dependency)
            val configuration = createNativeConfiguration(dependency) {
                exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
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
            val downloadedLibraries = loadAll("-${target.get().presetName}").map {
                it.copy(name = it.name)
            }

            val librariesJson = JsonOutput.toJson(
                downloadedLibraries.map {
                    mapOf(
                        "group" to it.group,
                        "name" to it.name,
                    )
                },
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
            client.post("https://central.sonatype.com/api/internal/browse/components") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {"size": 20, "page": $fromPage, "searchTerm": "$query", "filter": []}
                """.trimIndent(),
                )
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

    private fun createNativeConfiguration(vararg dependencies: Dependency, configure: Configuration.() -> Unit = { }): Configuration {
        return project.configurations.detachedConfiguration(*dependencies).apply {
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

    private fun String.removeSuffixIgnoringCase(suffix: String): String {
        return if (endsWith(suffix, ignoreCase = true)) {
            dropLast(suffix.length)
        } else {
            this
        }
    }
}
