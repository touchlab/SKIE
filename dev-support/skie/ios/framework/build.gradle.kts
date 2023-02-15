import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import co.touchlab.skie.configuration.gradle.ExperimentalFeatures
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
    ios()

    val exportedLibrary = "tz.co.asoft:result-core-iosarm64:0.0.50"

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.devSupport.skie.ios.dependency)

//                export(exportedLibrary)
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.skie:configuration-annotations")

            api(projects.devSupport.skie.ios.dependency)

//            api(exportedLibrary)
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("co.touchlab.skie:skie-kotlin-plugin")).using(module("co.touchlab.skie:kotlin-plugin:${version}"))
        substitute(module("co.touchlab.skie:skie-runtime-kotlin")).using(module("co.touchlab.skie:kotlin:${version}"))
    }
}
