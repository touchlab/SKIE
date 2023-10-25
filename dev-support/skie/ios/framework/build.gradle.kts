import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.ExperimentalFeatures
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("dev.multiplatform")

    id("co.touchlab.skie")
}

skie {
//     isEnabled = false
    analytics {
//        enabled = false
        disableUpload = true
    }

    build {
        enableParallelSwiftCompilation = true
    }

    features {
       defaultArgumentsInExternalLibraries = true
//         coroutinesInterop.set(false)
        group {
            ExperimentalFeatures.Enabled(true)
            DefaultArgumentInterop.Enabled(true)
        }
    }

    debug {
        printSkiePerformanceLogs.set(true)
        crashOnSoftErrors.set(true)
    }
}

kotlin {
    ios()
    iosSimulatorArm64()

    val exportedLibraries = listOf<String>(
//         "com.soywiz.korge:korge-core:5.0.6"
    )

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.devSupport.skie.ios.dependency)

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

            api(projects.devSupport.skie.ios.dependency)

            exportedLibraries.forEach {
                api(it)
            }
        }
    }

    val iosMain by sourceSets.getting
    val iosTest by sourceSets.getting

    val iosSimulatorArm64Main by sourceSets.getting {
        dependsOn(iosMain)
    }
    val iosSimulatorArm64Test by sourceSets.getting {
        dependsOn(iosTest)
    }

}
