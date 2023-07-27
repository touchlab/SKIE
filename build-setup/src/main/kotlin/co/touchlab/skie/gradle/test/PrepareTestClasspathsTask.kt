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
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File
import javax.inject.Inject

abstract class PrepareTestClasspathsTask: DefaultTask() {

    @get:Input
    abstract val target: Property<KonanTarget>

    @get:Input
    abstract val kotlinVersion: Property<String>

    @get:InputFile
    abstract val libraryLockFile: RegularFileProperty

    @get:OutputFile
    abstract val testInputJsonFile: RegularFileProperty

    @get:OutputDirectory
    abstract val libraryCacheDirectory: DirectoryProperty

    fun configureRuntimeDependencies() {
        dependsOn(
            runtimeKotlinCompileTask(),
            configurationAnnotationsCompileTask(),
        )
    }


    @TaskAction
    fun downloadAndStoreLibraries() {
        val resolutionProjectDir = libraryCacheDirectory.dir("resolve").get().asFile
        libraryLockFile.get().asFile.copyTo(resolutionProjectDir.resolve("input"), overwrite = true)

        runSubGradle(
            projectDir = resolutionProjectDir,
            imports = listOf(
                "org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget",
                "org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask",
                "org.jetbrains.kotlin.gradle.plugin.mpp.Framework",
                "groovy.json.JsonOutput",
            ),
            tasks = listOf("downloadAllArtifacts"),
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
                        project.configurations.named(this.name + "DebugFrameworkIosFat").configure {
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
            tasks.register("downloadAllArtifacts") {
                val nativeTargets = kotlin.targets.filterIsInstance<KotlinNativeTarget>()
                // dependsOn(
                //     nativeTargets.flatMap { it.binaries.map { it.linkTaskProvider } }
                // )
                doLast {
                    val json = libraries.withIndex().associate { (index, library) ->
                        val target = kotlin.targets.getByName("library_${'$'}index") as KotlinNativeTarget
                        val framework = target.binaries.filterIsInstance<Framework>().single()
                        library to mapOf(
                            "files" to framework.linkTask.libraries.filter { (it.extension == "klib" || it.isDirectory) && it.exists() }.map { it.absolutePath },
                            "exported-files" to framework.linkTask.exportLibraries.filter { (it.extension == "klib" || it.isDirectory) && it.exists() }.map { it.absolutePath },
                        )
                    }
                    project.file("output").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
                }
            }
            """.trimIndent(),
        )

        val json = JsonSlurper().parse(resolutionProjectDir.resolve("output")) as Map<String, Map<String, List<String>>>

        val runtimeKotlinKlib = runtimeKotlinCompileTask().get().outputFile.get().absolutePath
        val configurationAnnotationsKlib = configurationAnnotationsCompileTask().get().outputFile.get().absolutePath

        val updatedJson = json.mapValues { (library, artifacts) ->
            val cacheDirectory = libraryCacheDirectory.dir(library.replace(":", "/")).get().asFile
            val files = artifacts["files"] ?: emptyList()
            val exportedFiles = artifacts["exported-files"] ?: emptyList()
            val filePathMap = files.withIndex().associate { (index, path) ->
                val oldFile = File(path)
                val newFile = cacheDirectory.resolve("${index}-${oldFile.name}")

                path to newFile
            }

            filePathMap.forEach { (oldPath, newFile) ->
                File(oldPath).copyTo(newFile, overwrite = true)
            }

            mapOf(
                "files" to filePathMap.values.map { it.absolutePath }.sorted() + configurationAnnotationsKlib + runtimeKotlinKlib,
                "exported-files" to exportedFiles.map { filePathMap.getValue(it).absolutePath } +  runtimeKotlinKlib,
            )
        }

        testInputJsonFile.get().asFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(updatedJson)))
    }

    private fun runSubGradle(
        projectDir: File,
        imports: List<String> = emptyList(),
        tasks: List<String> = emptyList(),
        @Language("kotlin") code: String,
    ) {
        projectDir.resolve("build.gradle.kts").writeText(imports.joinToString("\n") { "import $it" } + """
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
        """.trimMargin() + "\n\n" + code)
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("gradle.properties").writeText("""
            org.gradle.jvmargs=-Xmx8g -XX:+UseParallelGC
            org.gradle.parallel=true
            kotlin.native.cacheKind=none
        """.trimIndent())
        projectDir.resolve("src/commonMain/kotlin/Empty.kt").apply {
            parentFile.mkdirs()
            writeText("""
                package co.touchlab.skie.test
            """.trimIndent())
        }

        GradleConnector.newConnector()
            .forProjectDirectory(projectDir)
            .connect().use { connection ->
                //obtain some information from the build
                val environment = connection.model(BuildEnvironment::class.java).get()

                //run some tasks
                connection.newBuild()
                    .forTasks(*tasks.toTypedArray())
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
            }
    }

    private fun runtimeKotlinCompileTask(): Provider<KotlinNativeCompile> = kotlinCompileTask(
        projectName = ":runtime:runtime-kotlin",
        targetName = "${target.get().presetName}__kgp_${kotlinVersion.get()}",
    )

    private fun configurationAnnotationsCompileTask(): Provider<KotlinNativeCompile> = kotlinCompileTask(
        projectName = ":common:configuration:configuration-annotations",
        targetName = target.get().presetName,
    )

    private fun kotlinCompileTask(projectName: String, targetName: String): Provider<KotlinNativeCompile> {
        val dependencyProject = project.project(projectName)
        val target = dependencyProject.extensions.getByType<KotlinMultiplatformExtension>().targets.named<KotlinNativeTarget>(targetName)
        val mainCompilation = target.flatMap { it.compilations.named("main") }
        return mainCompilation.flatMap { it.compileTaskProvider }
    }
}
