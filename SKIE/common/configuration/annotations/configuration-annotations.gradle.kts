@file:Suppress("invisible_reference", "invisible_member")
import co.touchlab.skie.buildsetup.tasks.BuildNestedGradle
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.tooling.GradleConnector
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages

plugins {
    id("skie.runtime")
//     id("skie.publishable")
}

// skiePublishing {
//     name = "SKIE Configuration Annotations"
//     description = "Annotations to configure SKIE behavior."
//     publishSources = true
// }

enum class SupportedAnnotationTarget(
    val targetName: String,
    val platformType: KotlinPlatformType = KotlinPlatformType.native,
) {
    Jvm("jvm", KotlinPlatformType.jvm),
    Js("js", KotlinPlatformType.js),
    AndroidNativeArm32("android_native_arm32"),
    AndroidNativeArm64("android_native_arm64"),
    AndroidNativeX86("android_native_x86"),
    AndroidNativeX64("android_native_x64"),

    IosArm32("ios_arm32"),
    IosArm64("ios_arm64"),
    IosX64("ios_x64"),
    IosSimulatorArm64("ios_simulator_arm64"),

    WatchosArm32("watchos_arm32"),
    WatchosArm64("watchos_arm64"),
    WatchosX86("watchos_x86"),
    WatchosX64("watchos_x64"),
    WatchosSimulatorArm64("watchos_simulator_arm64"),
    WatchosDeviceArm64("watchos_device_arm64"),

    TvosArm64("tvos_arm64"),
    TvosX64("tvos_x64"),
    TvosSimulatorArm64("tvos_simulator_arm64"),

    MacosX64("macos_x64"),
    MacosArm64("macos_arm64"),

    LinuxArm64("linux_arm64"),
    LinuxArm32Hfp("linux_arm32_hfp"),
    LinuxX64("linux_x64"),

    MingwX64("mingw_x64"),
    MingwX86("mingw_x86"),

    Wasm32("wasm32"),
}

val kotlin_1_8_0 = KotlinToolingVersion("1.8.0")
val kotlin_1_8_20 = KotlinToolingVersion("1.8.20")
val kotlin_1_9_0 = KotlinToolingVersion("1.9.0")
val kotlin_1_9_20 = KotlinToolingVersion("1.9.20")
val kotlin_2_0_0 = KotlinToolingVersion("2.0.0")

fun SupportedAnnotationTarget.getTargetDeclaration(kotlinVersion: KotlinToolingVersion): String? {
    return when (this) {
        SupportedAnnotationTarget.Jvm -> "jvm()"
        SupportedAnnotationTarget.Js -> if (kotlinVersion >= kotlin_1_9_0) {
            """
            js {
                browser()
                nodejs()
            }
            """.trimIndent()
        } else {
            """
            js(BOTH) {
                browser()
                nodejs()
            }
            """.trimIndent()
        }
        SupportedAnnotationTarget.AndroidNativeArm32 -> "androidNativeArm32()"
        SupportedAnnotationTarget.AndroidNativeArm64 -> "androidNativeArm64()"
        SupportedAnnotationTarget.AndroidNativeX86 -> "androidNativeX86()"
        SupportedAnnotationTarget.AndroidNativeX64 -> "androidNativeX64()"
        SupportedAnnotationTarget.IosArm32 -> "iosArm32()".takeUnless { kotlinVersion >= kotlin_1_9_20 }
        SupportedAnnotationTarget.IosArm64 -> "iosArm64()"
        SupportedAnnotationTarget.IosX64 -> "iosX64()"
        SupportedAnnotationTarget.IosSimulatorArm64 -> "iosSimulatorArm64()"
        SupportedAnnotationTarget.WatchosArm32 -> "watchosArm32()"
        SupportedAnnotationTarget.WatchosArm64 -> "watchosArm64()"
        SupportedAnnotationTarget.WatchosX86 -> "watchosX86()".takeUnless { kotlinVersion >= kotlin_1_9_20 }
        SupportedAnnotationTarget.WatchosX64 -> "watchosX64()"
        SupportedAnnotationTarget.WatchosSimulatorArm64 -> "watchosSimulatorArm64()"
        SupportedAnnotationTarget.WatchosDeviceArm64 -> "watchosDeviceArm64()"
        SupportedAnnotationTarget.TvosArm64 -> "tvosArm64()"
        SupportedAnnotationTarget.TvosX64 -> "tvosX64()"
        SupportedAnnotationTarget.TvosSimulatorArm64 -> "tvosSimulatorArm64()"
        SupportedAnnotationTarget.MacosX64 -> "macosX64()"
        SupportedAnnotationTarget.MacosArm64 -> "macosArm64()"
        SupportedAnnotationTarget.LinuxArm64 -> "linuxArm64()"
        SupportedAnnotationTarget.LinuxArm32Hfp -> "linuxArm32Hfp()"
        SupportedAnnotationTarget.LinuxX64 -> "linuxX64()"
        SupportedAnnotationTarget.MingwX64 -> "mingwX64()"
        SupportedAnnotationTarget.MingwX86 -> "mingwX86()".takeUnless { kotlinVersion >= kotlin_1_9_20 }
        SupportedAnnotationTarget.Wasm32 -> "wasm32()".takeUnless { kotlinVersion >= kotlin_1_9_20 }
    }
}


val smokeTestTmpRepositoryPath: String? by project
val publishTaskNames = listOfNotNull(
    "publishToMavenLocal" to listOf("publishToMavenLocal"),
    "publishToSonatype" to listOf("findSonatypeStagingRepository", "publishToSonatype"),
    if (smokeTestTmpRepositoryPath != null) {
        "publishAllPublicationsToSmokeTestTmpRepository" to listOf("publishAllPublicationsToSmokeTestTmpRepository")
    } else {
        null
    },
)

val publishTaskNamesWithTasks = publishTaskNames.associateWith { (publishTaskName, _) ->
    tasks.register(publishTaskName)
}

fun setupRootTasks() {
    val rootKotlinVersion = kotlin_1_8_20
    val supportedTargetsWithDeclarations = SupportedAnnotationTarget.values().mapNotNull { target ->
        target.getTargetDeclaration(rootKotlinVersion)?.let {
            target to it
        }
    }
    val copyRootProjectTask = tasks.register<Copy>("copyProject__root") {
        from(layout.projectDirectory.dir("impl")) {
            include("src/**", "build.gradle.kts", "gradle.properties", "settings.gradle.kts")
            filter(
                ReplaceTokens::class,
                "tokens" to mapOf(
                    "targetKotlinVersion" to rootKotlinVersion.toString(),
                    "artifactIdSuffix" to "",
                    "targets" to supportedTargetsWithDeclarations.joinToString("\n") { (_, declaration) -> declaration },
                    // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
                    // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
                    "dependencies" to if (rootKotlinVersion == kotlin_1_8_0) {
                        "implementation(libs.kotlinx.coroutines.core.legacy)"
                    } else {
                        "implementation(libs.kotlinx.coroutines.core)"
                    },
                    "smokeTestTmpRepositoryConfiguration" to smokeTestTmpRepositoryPath?.let {
                        """
                            publishing {
                                repositories {
                                    maven {
                                        url = uri("$it")
                                        name = "smokeTestTmp"
                                    }
                                }
                            }
                        """.trimIndent()
                    }.orEmpty(),
                )
            )
        }
        into(layout.buildDirectory.dir("configuration_annotations_impl_root"))
    }

    publishTaskNamesWithTasks.forEach { (publishTaskNames, parentPublishTask) ->
        val (publishTaskName, publishTasks) = publishTaskNames
        val publishTask = tasks.register<BuildNestedGradle>("${publishTaskName}__root") {
            group = "publishing"

            dependsOn(copyRootProjectTask)

            projectDir.fileProvider(copyRootProjectTask.map { it.destinationDir })

            tasks.set(publishTasks)
        }

        parentPublishTask.configure {
            dependsOn(publishTask)
        }
    }
}

setupRootTasks()

kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
    val pathSafeKotlinVersionName = kotlinToolingVersion.primaryVersion.toString().replace('.', '_')
    val supportedTargetsWithDeclarations = SupportedAnnotationTarget.values().mapNotNull { target ->
        target.getTargetDeclaration(kotlinToolingVersion.primaryVersion)?.let {
            target to it
        }
    }

    val copyProjectTask = tasks.register<Copy>("copyProject__kgp_${kotlinToolingVersion.primaryVersion}") {
        description = "Copies implementation for Kotlin ${kotlinToolingVersion.primaryVersion}."

        from(layout.projectDirectory.dir("impl")) {
            include("src/**", "build.gradle.kts", "gradle.properties", "settings.gradle.kts")
            filter(
                ReplaceTokens::class,
                "tokens" to mapOf(
                    "targetKotlinVersion" to kotlinToolingVersion.primaryVersion.toString(),
                    "artifactIdSuffix" to "-${kotlinToolingVersion.primaryVersion}",
                    "targets" to supportedTargetsWithDeclarations.joinToString("\n") { (_, declaration) -> declaration },
                    // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
                    // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
                    "dependencies" to if (kotlinToolingVersion.primaryVersion == kotlin_1_8_0) {
                        "implementation(libs.kotlinx.coroutines.core.legacy)"
                    } else {
                        "implementation(libs.kotlinx.coroutines.core)"
                    },
                    "smokeTestTmpRepositoryConfiguration" to smokeTestTmpRepositoryPath?.let {
                        """
                            publishing {
                                repositories {
                                    maven {
                                        url = uri("$it")
                                        name = "smokeTestTmp"
                                    }
                                }
                            }
                        """.trimIndent()
                    }.orEmpty(),
                )
            )
        }
        into(layout.buildDirectory.dir("configuration_annotations_impl_$pathSafeKotlinVersionName"))
    }

    val buildTask = tasks.register<BuildNestedGradle>("buildProject__kgp_${kotlinToolingVersion.primaryVersion}") {
        group = "build"

        dependsOn(copyProjectTask)

        projectDir.fileProvider(copyProjectTask.map { it.destinationDir })

        tasks.set(
            supportedTargetsWithDeclarations.flatMap { (target, _) ->
                listOf(
                    "generateMetadataFileFor${target.name}Publication",
                    "generatePomFileFor${target.name}Publication",
                )
            } + listOf(
                "generateMetadataFileForKotlinMultiplatformPublication",
                "generatePomFileForKotlinMultiplatformPublication",
            )
        )
    }

    publishTaskNamesWithTasks.forEach { (publishTaskNames, parentPublishTask) ->
        val (publishTaskName, publishTasks) = publishTaskNames
        val publishTask = tasks.register<BuildNestedGradle>("${publishTaskName}__kgp_${kotlinToolingVersion.primaryVersion}") {
            group = "publishing"

            dependsOn(copyProjectTask)

            projectDir.fileProvider(copyProjectTask.map { it.destinationDir })

            tasks.set(publishTasks)
        }

        parentPublishTask.configure {
            dependsOn(publishTask)
        }
    }


//     if (kotlinToolingVersion.primaryVersion == kotlin_2_0_0) {
//         val configuration = configurations.create("jvm__kgp_${kotlinToolingVersion.primaryVersion}") {
//             isCanBeConsumed = true
//             isCanBeResolved = false
//
//             attributes {
//                 attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
//                 attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
//                 attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
//                 attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
// //                 attribute(KotlinNativeTarget.konanTargetAttribute, target.targetName)
//                 attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.primaryVersion.toString()))
//             }
//         }
//
//         val klibPath = "impl_$pathSafeKotlinVersionName/build/classes/kotlin/jvm/main/klib/skie-kotlin-runtime.klib"
//
//         artifacts.add(configuration.name, layout.buildDirectory.file(klibPath)) {
//             builtBy(buildTask)
//
//         }
//     }

//     if (kotlinToolingVersion.primaryVersion == kotlin_2_0_0) {
//         val metadataConfiguration = configurations.create("metadata__kgp_${kotlinToolingVersion.primaryVersion}") {
//             isCanBeConsumed = true
//             isCanBeResolved = false
//
//             attributes {
//                 attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_METADATA))
//                 attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
//                 attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
//             }
//         }
//
//         val metadataArtifactPath = "configuration_annotations_impl_$pathSafeKotlinVersionName/build/libs/skie-configuration-annotations-${kotlinToolingVersion.primaryVersion}-metadata-${version}.jar"
//         artifacts.add(metadataConfiguration.name, layout.buildDirectory.file(metadataArtifactPath)) {
//             builtBy(buildTask)
//         }
//
//         supportedTargetsWithDeclarations.forEach { (target, _) ->
//             val lowercaseTargetName = target.name.replaceFirstChar { it.lowercase() }
//             val configuration = configurations.create("${lowercaseTargetName}__kgp_${kotlinToolingVersion.primaryVersion}") {
//                 isCanBeConsumed = true
//                 isCanBeResolved = false
//
//                 attributes {
// //                 attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_RUNTIME))
//                     attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
//                     attribute(KotlinPlatformType.attribute, target.platformType)
//                     attribute(
//                         TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
//                         objects.named(
//                             if (target.platformType == KotlinPlatformType.jvm) {
//                                 TargetJvmEnvironment.STANDARD_JVM
//                             } else {
//                                 "non-jvm"
//                             }
//                         )
//                     )
//                     attribute(KotlinNativeTarget.konanTargetAttribute, target.targetName)
//                     if (target.platformType != KotlinPlatformType.jvm) {
//                         attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.primaryVersion.toString()))
//                     }
//                 }
//             }
//
//             val artifactPath = if (target.platformType == KotlinPlatformType.jvm) {
//                 "configuration_annotations_impl_$pathSafeKotlinVersionName/build/libs/skie-configuration-annotations-${kotlinToolingVersion.primaryVersion}-${target.name.lowercase()}-${version}.jar"
//             } else {
//                 "configuration_annotations_impl_$pathSafeKotlinVersionName/build/classes/kotlin/${lowercaseTargetName}/main/klib/skie-configuration-annotations-${kotlinToolingVersion.primaryVersion}.klib"
//             }
//             artifacts.add(configuration.name, layout.buildDirectory.file(artifactPath)) {
//                 builtBy(buildTask)
//             }
//         }
//     }
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxArm64()
    linuxX64()

    mingwX64()

    sourceSets.commonMain {
        kotlin.srcDirs("impl/src/commonMain/kotlin")
    }
}

// afterEvaluate {
//     println("Components:")
//     components.forEach {
//         println("\t${it.name}")
//     }
// }
//
// val p = project
// project.launchInRequiredStage(KotlinPluginLifecycle.Stage.ReadyForExecution) {
//     println("Components: $p")
//     components.forEach { component ->
//         println("\t- ${component.name} - $component")
//     }
//
//     println("Publications:")
//     publishing.publications.forEach { publication ->
//         println("\t- ${publication.name} - $publication")
//     }
//
// }
