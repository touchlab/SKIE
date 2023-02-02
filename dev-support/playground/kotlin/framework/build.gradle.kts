import co.touchlab.skie.configuration.gradle.ExperimentalFeatures
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("skie-multiplatform")

    id("co.touchlab.skie")
}

skie {
    features {
//        fqNames.set(true)
    }
    configuration {
        group {
            ExperimentalFeatures.Enabled(true)
        }
    }
}

kotlin {
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
            implementation(libs.kotlinx.coroutines.core)

            implementation(projects.playground.kotlin.library)

            implementation("co.touchlab.skie:configuration-annotations")
        }
    }

    val macosMain by sourceSets.creating {
        dependsOn(commonMain)
    }

    val macosArm64Main by sourceSets.getting {
        dependsOn(macosMain)
    }

    val macosX64Main by sourceSets.getting {
        dependsOn(macosMain)
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("co.touchlab.skie:skie-kotlin-plugin")).using(module("co.touchlab.skie:kotlin-plugin:${version}"))
        substitute(module("co.touchlab.skie:skie-runtime-kotlin")).using(module("co.touchlab.skie:kotlin:${version}"))
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
