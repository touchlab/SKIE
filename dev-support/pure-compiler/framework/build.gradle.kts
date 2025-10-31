import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family

plugins {
    id("dev.multiplatform")
}

kotlin {
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    val exportedLibraries = listOf<String>(
    )

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = false
                baseName = "Kotlin"
                freeCompilerArgs += listOf("-Xbinary=bundleId=Kotlin")

                export(projects.devSupport.pureCompilerDependency)

                exportedLibraries.forEach {
                    export(it)
                }
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("co.touchlab.skie:configuration-annotations")

            api(projects.devSupport.pureCompilerDependency)

            exportedLibraries.forEach {
                api(it)
            }
        }
    }
}

tasks.withType<KotlinNativeLink>().configureEach {
    val provider = project.providers

    if (binary.target.konanTarget.family != Family.OSX) {
        doLast {
            val frameworkDirectory = outputs.files.toList().first()
            val apiFile = frameworkDirectory.resolve("KotlinApi.swift")

            provider.exec {
                commandLine(
                    "zsh",
                    "-c",
                    "echo \"import Kotlin\\n:type lookup Kotlin\" | swift repl -F \"${frameworkDirectory.absolutePath}\" > \"${apiFile.absolutePath}\"",
                )
            }
        }
    }
}
