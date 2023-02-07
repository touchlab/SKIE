package co.touchlab.skie.plugin.libraries

import org.gradle.testkit.runner.GradleRunner
import java.io.File

class ExternalLibrariesTestPreWarmer(private val testTmpDir: File) {

    private val testKitDir = testTmpDir.resolve("testkit")
    fun warmUp(): File {
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
            .withTestKitDir(testKitDir)
            .forwardOutput()
            .withArguments(
                "linkDebugFrameworkIosArm64",
                "--stacktrace",
                "-Dorg.gradle.jvmargs=-Xmx4g",
            )
            .withPluginClasspath()
            .build()

        return testKitDir
    }
}
