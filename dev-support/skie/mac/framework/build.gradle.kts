import co.touchlab.skie.configuration.ExperimentalFeatures
// import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("dev.multiplatform")

    id("co.touchlab.skie")
}

skie {
    analytics {
        disableUpload.set(true)
    }

    build {
        enableParallelSwiftCompilation = true
    }

    features {
        group {
            ExperimentalFeatures.Enabled(true)
        }
    }

    debug {
        dumpSwiftApiBeforeApiNotes.set(true)
        dumpSwiftApiAfterApiNotes.set(true)
        printSkiePerformanceLogs.set(true)
        crashOnSoftErrors.set(true)
    }
}

kotlin {
    macosX64()
    macosArm64()

    val exportedLibraries = listOf<String>(

    )

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.skie.mac.dependency)

                exportedLibraries.forEach {
                    export(it)
                }
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
//             implementation("co.touchlab.skie:configuration-annotations")
//             implementation("co.touchlab.skie:kotlin")

            api(projects.skie.mac.dependency)

            exportedLibraries.forEach {
                api(it)
            }
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

// configurations.all {
//     resolutionStrategy.dependencySubstitution {
//         substitute(module("co.touchlab.skie:skie-kotlin-plugin")).using(module("co.touchlab.skie:kotlin-plugin:${version}"))
//         substitute(module("co.touchlab.skie:skie-runtime-kotlin")).using(module("co.touchlab.skie:kotlin:${version}"))
//     }
// }

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

// tasks.register("dependenciesForExport") {
//     doLast {
//         val configuration = configurations.getByName(MacOsCpuArchitecture.getCurrent().kotlinGradleName + "Api")
//
//         val dependencies = configuration.incoming.resolutionResult.allComponents.map { it.toString() }
//         val externalDependencies = dependencies.filterNot { it.startsWith("project :") }
//
//         externalDependencies.forEach {
//             println("export(\"$it\")")
//         }
//     }
// }
