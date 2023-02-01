package co.touchlab.skie.plugin

import groovy.json.JsonSlurper
import org.gradle.internal.impldep.org.testng.Assert
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildResultException
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.time.ExperimentalTime

class ExternalLibrariesTest {

    val testTmpDir = File(System.getProperty("testTmpDir"))

    @Test
    fun testExternalLibraries() {
        val allLibrariesFile = this::class.java.getResource("/libraries-to-test.json")
        val allLibraries = JsonSlurper().parse(allLibrariesFile) as Map<String, List<String>>

        warmUpTestKit()

        val onlyIndices = setOf<Int>(
        )

        val librariesToTest = allLibraries.toList()
            .mapIndexed { index, (library, exportedLibraries) ->
                index to (library to exportedLibraries)
            }
            .filter { onlyIndices.isEmpty() || it.first in onlyIndices }

        val rootDir = prepareRootDir()

        librariesToTest.forEach { (index, item) ->
            val (library, exportedLibraries) = item
            prepareLibraryDir(rootDir, index, library, exportedLibraries)
        }

        rootDir.resolve("settings.gradle.kts").appendText(
            "\n\n" + librariesToTest.map { (index, _) ->
                "include(\":library-${index}\")"
            }.joinToString("\n")
        )

        val result = try {
            GradleRunner.create()
                .withProjectDir(rootDir)
                .withTestKitDir(testTmpDir.resolve("testkit"))
                .withArguments(
                    "linkDebugFrameworkIosArm64",
                    "--stacktrace",
                    "--continue",
                    "-Dorg.gradle.jvmargs=-Xmx20g",
                    "-Dorg.gradle.parallel=true",
                )
                .withPluginClasspath()
                .build()
        } catch (e: UnexpectedBuildResultException) {
            e.buildResult
        }

        rootDir.resolve("run.log").writeText(result.output)

        val taskOutcomes = librariesToTest.map { (index, item) ->
            val (library, _) = item
            val outcome = result.task(":library-${index}:linkDebugFrameworkIosArm64")?.outcome ?: TaskOutcome.SKIPPED
            index to (library to outcome)
        }

        taskOutcomes.forEach { (index, item) ->
            val (library, outcome) = item
            println("[${outcome}] for $library ($index)")
        }

        val failures = taskOutcomes.filter { (_, item) ->
            val (_, outcome) = item
            outcome != TaskOutcome.SUCCESS
        }

        if (failures.isNotEmpty()) {
            println("To run only failed tests:")
            println(failures.joinToString(", ") { "${it.first}" })
            Assert.fail("${failures.size} failed out of ${librariesToTest.size}")
        }
    }

    private fun warmUpTestKit() {
        val buildDir = testTmpDir.resolve("warmup").apply {
            mkdirs()
        }

        buildDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "external-libraries-test-warmup"

            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                    mavenLocal()
                    maven("https://api.touchlab.dev/public") {
                        content {
                            includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
                            includeModule("co.touchlab.fork.swiftpoet", "swiftpoet")
                        }
                    }
                }
            }
        """.trimIndent())

        buildDir.resolve("src/commonMain/kotlin/co/touchlab/skie/test").apply {
            mkdirs()
            resolve("EmptySource.kt").writeText("""
                package co.touchlab.skie.test
            """.trimIndent())
        }

        buildDir.resolve("build.gradle.kts").writeText(
            """
                    import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

                    plugins {
                        kotlin("multiplatform") version "1.7.20"
                        id("co.touchlab.skie")
                    }

                    kotlin {
                        iosArm64()

                        targets.withType<KotlinNativeTarget> {
                            binaries {
                                framework {
                                    isStatic = true
                                    baseName = "Kotlin"
                                    freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")
                                }
                            }
                        }
                    }
                    """.trimIndent()
        )

        GradleRunner.create()
            .withProjectDir(buildDir)
            .withTestKitDir(testTmpDir.resolve("testkit"))
            .withArguments(
                "tasks",
                "--stacktrace",
                "-Dorg.gradle.jvmargs=-Xmx4g",
            )
            .withPluginClasspath()
            .build()
    }

    private fun prepareRootDir(): File {
        val rootDir = testTmpDir.resolve("root").apply {
            mkdirs()
        }

        rootDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "external-libraries-test"

            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                    mavenLocal()
                    maven("https://api.touchlab.dev/public") {
                        content {
                            includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
                            includeModule("co.touchlab.fork.swiftpoet", "swiftpoet")
                        }
                    }
                }
            }
        """.trimIndent())

        return rootDir
    }

    private fun prepareLibraryDir(rootDir: File, index: Int, library: String, exportedLibraries: List<String>): File {
        val buildDir = rootDir.resolve("library-$index").apply {
            mkdirs()
        }

        buildDir.resolve("src/commonMain/kotlin/co/touchlab/skie/test").apply {
            mkdirs()
            resolve("EmptySource.kt").writeText("""
                package co.touchlab.skie.test
            """.trimIndent())
        }

        buildDir.resolve("build.gradle.kts").writeText(
            """
                    import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

                    plugins {
                        kotlin("multiplatform") version "1.7.20"
                        id("co.touchlab.skie")
                    }

                    kotlin {
                        iosArm64()

                        sourceSets {
                            val commonMain by getting {
                                dependencies {
                                    api("$library")
                                }
                            }
                        }

                        targets.withType<KotlinNativeTarget> {
                            binaries {
                                framework {
                                    isStatic = true
                                    baseName = "Kotlin"
                                    freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                                    ${
                                        exportedLibraries.joinToString("\n") {
                                            """                                export("$it")"""
                                        }
                                    }
                                }
                            }
                        }
                    }
                    """.trimIndent()
        )

        return buildDir
    }
}
