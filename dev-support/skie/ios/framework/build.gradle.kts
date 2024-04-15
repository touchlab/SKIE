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

                export(projects.devSupport.skieIosDependency)

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

            api(projects.devSupport.skieIosDependency)

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

val performanceBenchmarkLibraries = listOf(
    "org.hildan.chrome:chrome-devtools-kotlin-iosarm64:5.6.0-1207450",
    "com.soywiz.korlibs.korge2:korge-box2d-iosarm64:3.3.0",
    "com.soywiz.korlibs.kbox2d:kbox2d-iosarm64:3.3.0",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3",
    "io.ktor:ktor-client-core-iosarm64:2.3.5",
    "co.touchlab:kermit-iosarm64:2.0.2",
    "com.squareup.sqldelight:runtime-iosarm64:1.5.5",
    "com.russhwolf:multiplatform-settings-iosarm64:1.1.0",
    "io.insert-koin:koin-core-iosarm64:3.5.0",
    "org.jetbrains.kotlinx:kotlinx-serialization-core-iosarm64:1.6.0",
    "com.splendo.kaluga:alerts-iosarm64:1.2.1",
    "com.splendo.kaluga:architecture-iosarm64:1.2.1",
    "com.splendo.kaluga:base-iosarm64:1.2.1",
    "com.splendo.kaluga:base-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:beacons-iosarm64:1.2.1",
    "com.splendo.kaluga:bluetooth-iosarm64:1.2.1",
    "com.splendo.kaluga:bluetooth-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:calendar-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:camera-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:contacts-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:date-time-iosarm64:1.2.1",
    "com.splendo.kaluga:date-time-picker-iosarm64:1.2.1",
    "com.splendo.kaluga:hud-iosarm64:1.2.1",
    "com.splendo.kaluga:keyboard-iosarm64:1.2.1",
    "com.splendo.kaluga:links-iosarm64:1.2.1",
    "com.splendo.kaluga:location-iosarm64:1.2.1",
    "com.splendo.kaluga:location-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:logging-iosarm64:1.2.1",
    "com.splendo.kaluga:media-iosarm64:1.2.1",
    "com.splendo.kaluga:microphone-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:notifications-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:resources-iosarm64:1.2.1",
    "com.splendo.kaluga:review-iosarm64:1.2.1",
    "com.splendo.kaluga:scientific-iosarm64:1.2.1",
    "com.splendo.kaluga:service-iosarm64:1.2.1",
    "com.splendo.kaluga:storage-permissions-iosarm64:1.2.1",
    "com.splendo.kaluga:system-iosarm64:1.2.1",
)
