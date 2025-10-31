import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.ExperimentalFeatures
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("dev.multiplatform")

    id("co.touchlab.skie")
}

skie {
//     isEnabled.set(false)
    analytics {
//        enabled = false
        disableUpload = true
    }

    build {
        enableParallelSwiftCompilation = true
    }
    features {
//        defaultArgumentsInExternalLibraries = true
//         coroutinesInterop.set(false)
        group {
            ExperimentalFeatures.Enabled(true)
            DefaultArgumentInterop.Enabled(true)
            ClassInterop.StableTypeAlias(true)
        }
    }

    debug {
//        dumpSwiftApiBeforeApiNotes.set(true)
//        dumpSwiftApiAfterApiNotes.set(true)
        printSkiePerformanceLogs.set(true)
        crashOnSoftErrors.set(true)
        loadAllPlatformApiNotes.set(true)
        generateFileForEachExportedClass.set(true)
    }
}

val exportedLibraries = listOf<String>(

)

kotlin {
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.skieMacDependency)

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

            api(projects.skieMacDependency)

            exportedLibraries.forEach {
                api(it)
            }
        }
    }
}

kotlinArtifacts {
    Native.Framework("Kotlin") {
        target = macosArm64
        isStatic = true
        toolOptions {
            freeCompilerArgs.add("-Xbinary=bundleId=Kotlin")
        }

        exportedLibraries.forEach {
            addModule(it)
        }
    }
    Native.XCFramework("Kotlin") {
        targets = setOf(macosArm64, macosX64)
        isStatic = true
        toolOptions {
            freeCompilerArgs.add("-Xbinary=bundleId=Kotlin")
        }

        exportedLibraries.forEach {
            addModule(it)
        }
    }
}
