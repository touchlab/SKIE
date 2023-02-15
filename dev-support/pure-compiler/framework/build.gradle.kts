import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture

plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()
    macosX64()
    macosArm64()

    val exportedLibrary = "co.touchlab:kmmworker-iosarm64:0.1.1"

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.devSupport.pureCompiler.dependency)

//                export(exportedLibrary)
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.skie:configuration-annotations")

            api(projects.devSupport.pureCompiler.dependency)

//            api(exportedLibrary)
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
