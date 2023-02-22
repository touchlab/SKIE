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

    val exportedLibraries = listOf<String>(

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
            implementation("co.touchlab.skie:kotlin")

            api(projects.devSupport.skie.ios.dependency)

            exportedLibraries.forEach {
                api(it)
            }
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("co.touchlab.skie:skie-kotlin-plugin")).using(module("co.touchlab.skie:kotlin-plugin:${version}"))
        substitute(module("co.touchlab.skie:skie-runtime-kotlin")).using(module("co.touchlab.skie:kotlin:${version}"))
    }
}
