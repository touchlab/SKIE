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

//                export("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
//                export("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosarm64:1.6.4")
//                export("org.jetbrains.kotlin:kotlin-stdlib-common:1.7.0")
//                export("org.jetbrains.kotlinx:atomicfu:0.17.3")
//                export("org.jetbrains.kotlinx:atomicfu-iosarm64:0.17.3")
//                export("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
//                export("org.jetbrains.kotlinx:kotlinx-datetime-iosarm64:0.4.0")
//                export("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
//                export("org.jetbrains.kotlinx:kotlinx-serialization-core-iosarm64:1.3.2")
//                export("com.adadapted:aamsdk:1.0.3")
//                export("com.adadapted:aamsdk-iosarm64:1.0.3")
//                export("io.ktor:ktor-client-core:2.0.1")
//                export("io.ktor:ktor-client-core-iosarm64:2.0.1")
//                export("io.ktor:ktor-http:2.0.1")
//                export("io.ktor:ktor-http-iosarm64:2.0.1")
//                export("io.ktor:ktor-utils:2.0.1")
//                export("io.ktor:ktor-utils-iosarm64:2.0.1")
//                export("io.ktor:ktor-io:2.0.1")
//                export("io.ktor:ktor-io-iosarm64:2.0.1")
//                export("io.ktor:ktor-events:2.0.1")
//                export("io.ktor:ktor-events-iosarm64:2.0.1")
//                export("io.ktor:ktor-websocket-serialization:2.0.1")
//                export("io.ktor:ktor-websocket-serialization-iosarm64:2.0.1")
//                export("io.ktor:ktor-serialization:2.0.1")
//                export("io.ktor:ktor-serialization-iosarm64:2.0.1")
//                export("io.ktor:ktor-websockets:2.0.1")
//                export("io.ktor:ktor-websockets-iosarm64:2.0.1")
//                export("io.ktor:ktor-serialization-kotlinx-json:2.0.1")
//                export("io.ktor:ktor-serialization-kotlinx-json-iosarm64:2.0.1")
//                export("io.ktor:ktor-serialization-kotlinx:2.0.1")
//                export("io.ktor:ktor-serialization-kotlinx-iosarm64:2.0.1")
//                export("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
//                export("org.jetbrains.kotlinx:kotlinx-serialization-json-iosarm64:1.3.2")
//                export("io.ktor:ktor-client-content-negotiation:2.0.1")
//                export("io.ktor:ktor-client-content-negotiation-iosarm64:2.0.1")
//                export("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.0")
//                export("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
//                export("org.jetbrains:annotations:13.0")
//                export("io.github.aakira:napier:2.6.1")
//                export("io.github.aakira:napier-iosarm64:2.6.1")
//                export("io.ktor:ktor-client-ios:2.0.1")
//                export("io.ktor:ktor-client-ios-iosarm64:2.0.1")
//                export("io.ktor:ktor-client-darwin:2.0.1")
//                export("io.ktor:ktor-client-darwin-iosarm64:2.0.1")
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

            api("com.adadapted:aamsdk:1.0.3")

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
