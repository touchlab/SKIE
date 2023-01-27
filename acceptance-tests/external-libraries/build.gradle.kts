import co.touchlab.skie.configuration.gradle.ExperimentalFeatures
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("skie-multiplatform")

    id("co.touchlab.skie")
}

skie {
    features {
//        suspendInterop.set(true)
        fqNames.set(true)
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

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                when (MacOsCpuArchitecture.getCurrent()) {
                    MacOsCpuArchitecture.Arm64 -> {
                        export("org.jetbrains.kotlin:kotlin-stdlib-common:1.7.21")
                        export("co.touchlab:stately-common:1.2.3")
                        export("co.touchlab:stately-common-macosarm64:1.2.3")
                        export("io.insert-koin:koin-core:3.2.1")
                        export("io.insert-koin:koin-core-macosarm64:3.2.1")
                        export("co.touchlab:stately-concurrency:1.2.3")
                        export("co.touchlab:stately-concurrency-macosarm64:1.2.3")
                        export("co.touchlab:kermit:1.2.2")
                        export("co.touchlab:kermit-macosarm64:1.2.2")
                        export("com.russhwolf:multiplatform-settings:1.0.0-RC")
                        export("com.russhwolf:multiplatform-settings-macosarm64:1.0.0-RC")
                        export("io.ktor:ktor-client-core:2.2.2")
                        export("io.ktor:ktor-client-core-macosarm64:2.2.2")
                        export("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                        export("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosarm64:1.6.4")
                        export("org.jetbrains.kotlinx:atomicfu:0.18.5")
                        export("org.jetbrains.kotlinx:atomicfu-macosarm64:0.18.5")
                        export("io.ktor:ktor-http:2.2.2")
                        export("io.ktor:ktor-http-macosarm64:2.2.2")
                        export("io.ktor:ktor-utils:2.2.2")
                        export("io.ktor:ktor-utils-macosarm64:2.2.2")
                        export("io.ktor:ktor-io:2.2.2")
                        export("io.ktor:ktor-io-macosarm64:2.2.2")
                        export("io.ktor:ktor-events:2.2.2")
                        export("io.ktor:ktor-events-macosarm64:2.2.2")
                        export("io.ktor:ktor-websocket-serialization:2.2.2")
                        export("io.ktor:ktor-websocket-serialization-macosarm64:2.2.2")
                        export("io.ktor:ktor-serialization:2.2.2")
                        export("io.ktor:ktor-serialization-macosarm64:2.2.2")
                        export("io.ktor:ktor-websockets:2.2.2")
                        export("io.ktor:ktor-websockets-macosarm64:2.2.2")
                        export("com.squareup.sqldelight:gradle-plugin:1.5.4")
                        export("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                        export("org.jetbrains.kotlinx:kotlinx-datetime-macosarm64:0.4.0")
                        export("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
                        export("org.jetbrains.kotlinx:kotlinx-serialization-core-macosarm64:1.4.1")
                        export("io.github.aakira:napier:2.6.1")
                        export("io.github.aakira:napier-macosarm64:2.6.1")
                        export("com.apollographql.apollo3:apollo-runtime:3.7.3")
                        export("com.apollographql.apollo3:apollo-runtime-macosarm64:3.7.3")
                        export("com.apollographql.apollo3:apollo-api:3.7.3")
                        export("com.apollographql.apollo3:apollo-api-macosarm64:3.7.3")
                        export("com.squareup.okio:okio:3.2.0")
                        export("com.squareup.okio:okio-macosarm64:3.2.0")
                        export("com.benasher44:uuid:0.6.0")
                        export("com.benasher44:uuid-macosarm64:0.6.0")
                        export("com.apollographql.apollo3:apollo-annotations:3.7.3")
                        export("com.apollographql.apollo3:apollo-annotations-macosarm64:3.7.3")
                        export("org.jetbrains.kotlin:kotlin-stdlib:1.7.20")
                        export("org.jetbrains:annotations:23.0.0")
                        export("com.apollographql.apollo3:apollo-mpp-utils:3.7.3")
                        export("com.apollographql.apollo3:apollo-mpp-utils-macosarm64:3.7.3")
                        export("com.juul.kable:core:0.20.1")
                        export("com.juul.kable:core-macosarm64:0.20.1")
                        export("com.juul.tuulbox:collections:6.3.0")
                        export("com.juul.tuulbox:collections-macosarm64:6.3.0")
                        export("co.touchlab:stately-iso-collections:1.2.3")
                        export("co.touchlab:stately-iso-collections-macosarm64:1.2.3")
                        export("co.touchlab:stately-isolate:1.2.3")
                        export("co.touchlab:stately-isolate-macosarm64:1.2.3")
                        export("org.kodein.di:kodein-di:7.16.0")
                        export("org.kodein.di:kodein-di-macosarm64:7.16.0")
                        export("org.kodein.type:kaverit:2.2.1")
                        export("org.kodein.type:kaverit-macosarm64:2.2.1")
                        export("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                        export("org.jetbrains.kotlinx:kotlinx-serialization-json-macosarm64:1.4.1")
                        export("com.badoo.reaktive:reaktive:1.2.2")
                        export("com.badoo.reaktive:reaktive-macosarm64:1.2.2")
                        export("com.badoo.reaktive:utils:1.2.2")
                        export("com.badoo.reaktive:utils-macosarm64:1.2.2")
                        export("com.badoo.reaktive:reaktive-annotations:1.2.2")
                        export("com.badoo.reaktive:reaktive-annotations-macosarm64:1.2.2")
                        export("com.soywiz.korlibs.klock:klock:3.4.0")
                        export("com.soywiz.korlibs.klock:klock-macosarm64:3.4.0")
                        export("com.github.kittinunf.result:result:5.3.0")
                        export("com.github.kittinunf.result:result-macosarm64:5.3.0")
                        export("io.arrow-kt:arrow-core:1.1.3")
                        export("io.arrow-kt:arrow-core-macosarm64:1.1.3")
                        export("io.arrow-kt:arrow-continuations:1.1.3")
                        export("io.arrow-kt:arrow-continuations-macosarm64:1.1.3")
                        export("org.codehaus.mojo:animal-sniffer-annotations:1.21")
                        export("io.arrow-kt:arrow-annotations:1.1.3")
                        export("io.arrow-kt:arrow-annotations-macosarm64:1.1.3")
                    }
                    MacOsCpuArchitecture.X64 -> TODO()
                }
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            api("co.touchlab.skie:configuration-annotations")

            api("co.touchlab:stately-common:1.2.3")
            api("io.insert-koin:koin-core:3.2.1")
            api("co.touchlab:kermit:1.2.2")
            api("com.russhwolf:multiplatform-settings:1.0.0-RC")
            api("io.ktor:ktor-client-core:2.2.2")
            api("com.squareup.sqldelight:gradle-plugin:1.5.4")
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            api("io.github.aakira:napier:2.6.1")
            api("com.apollographql.apollo3:apollo-runtime:3.7.3")
            api("com.juul.kable:core:0.20.1")
            api("org.kodein.di:kodein-di:7.16.0")
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            api("com.badoo.reaktive:reaktive:1.2.2")
            api("com.benasher44:uuid:0.6.0")
            api("com.soywiz.korlibs.klock:klock:3.4.0")
            api("com.github.kittinunf.result:result:5.3.0")
            api("com.squareup.okio:okio:parent-3.2.0")
            api("io.arrow-kt:arrow-core:1.1.3")

//            api("com.arkivanov.decompose:decompose:1.0.0-beta-03")
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

    dependsOn("linkDebugFramework${MacOsCpuArchitecture.getCurrent().kotlinGradleName.capitalized()}")
}

tasks.named("allTests") {
    dependsOn(test)
}
