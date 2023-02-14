import co.touchlab.skie.configuration.gradle.ExperimentalFeatures
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("skie-multiplatform")

    id("co.touchlab.skie")
}

skie {
    features {
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

    val exportedLibrary = "tz.co.asoft:result-core:0.0.50"

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.skie.mac.library)

//                export(exportedLibrary)
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.skie:configuration-annotations")

            api(projects.skie.mac.library)

//            api(exportedLibrary)
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

tasks.register("dependenciesForExport") {
    doLast {
        val configuration = configurations.getByName(MacOsCpuArchitecture.getCurrent().kotlinGradleName + "Api")

        val dependencies = configuration.incoming.resolutionResult.allComponents.map { it.toString() }
        val externalDependencies = dependencies.filterNot { it.startsWith("project :") }

        externalDependencies.forEach {
            println("export(\"$it\")")
        }
    }
}
