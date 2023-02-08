import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
//            ExperimentalFeatures.Enabled(true)
        }
    }
}

kotlin {
    ios()

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
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                version {
                    strictly("1.6.4")
                }
            }
            api("org.jetbrains.kotlinx:kotlinx-datetime") {
                version {
                    strictly("0.4.0")
                }
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

val test by tasks.registering {
    group = "verification"

    dependsOn("linkDebugFrameworkIosArm64")
}

tasks.named("allTests") {
    dependsOn(test)
}
