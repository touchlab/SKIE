package co.touchlab.skie.plugin.libraries

import java.io.File

class ExternalLibrariesTestDirPreparer(private val testTmpDir: File) {
    private val rootDir = testTmpDir.resolve("root").apply {
        mkdirs()
    }

    fun prepareRootDir(testedLibraries: List<ExternalLibraryTest>): File {
        val defaultContent = """
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
        """.trimIndent()

        val libraryIncludes = testedLibraries.joinToString("\n") { test ->
            """include(":${test.directoryName}")"""
        }

        rootDir.resolve("settings.gradle.kts").writeText(defaultContent + "\n\n" + libraryIncludes)

        return rootDir
    }

    fun prepareLibraryDirs(testedLibraries: List<ExternalLibraryTest>): List<File> {
        return testedLibraries.map { test ->
            prepareLibraryDir(test)
        }
    }

    private fun prepareLibraryDir(test: ExternalLibraryTest): File {
        val buildDir = rootDir.resolve(test.directoryName).apply {
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
                                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                                        version {
                                            strictly("1.6.4")
                                        }
                                    }
                                    api("org.jetbrains.kotlinx:kotlinx-datetime") {
                                        version {
                                            strictly("0.4.0")
                                        }
                                    }
                                    api("${test.library}")
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
                test.exportedLibraries.joinToString("\n") {
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
