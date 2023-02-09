import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(projects.devSupport.pureCompiler.library)
        }
    }
}

tasks.withType<KotlinNativeLink>().configureEach {
    doLast {
        val frameworkDirectory = outputs.files.toList().first()
        val apiFile = frameworkDirectory.resolve("KotlinApi.swift")

        exec {
            commandLine(
                "zsh",
                "-c",
                "echo \"import Kotlin\\n:type lookup Kotlin\" | swift repl -F \"${frameworkDirectory.absolutePath}\" > \"${apiFile.absolutePath}\"",
            )
        }
    }
}
