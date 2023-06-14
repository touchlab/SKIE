import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.publish.mavenArtifactId
import co.touchlab.skie.gradle.version.gradleApiVersions
import co.touchlab.skie.gradle.version.kotlinPluginShimVersions
import co.touchlab.skie.gradle.version.kotlinToolingVersions

plugins {
    id("skie.gradle")
    id("skie.publishable")
    id("dev.buildconfig")

//     alias(libs.plugins.kotlin.plugin.serialization)
}

buildConfig {
//     val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject
//     // TODO Rename to SKIE_GRADLE_PLUGIN
//     buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${kotlinPlugin.group}\"")
//     buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${kotlinPlugin.mavenArtifactId}\"")
//     buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${kotlinPlugin.version}\"")
//
//     val runtime = projects.runtime.runtimeKotlin.dependencyProject
//     buildConfigField("String", "RUNTIME_DEPENDENCY", "\"${runtime.dependencyName}\"")
//
//     val pluginId: String by properties
//     buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}

// val pluginImplementationForTests = configurations.create("pluginImplementationForTests")

kotlin {
    sourceSets {
        val commonMain by getting {
            kotlin.srcDirs(
                "src/common/kotlin-compiler-attribute",
                "src/common/shim-loader",
            )

            dependencies {
                implementation(projects.kotlinGradlePluginShim)
//
//                 implementation(projects.common.analytics)
//                 implementation(projects.common.configuration)
//                 implementation(projects.common.license)
//                 implementation(projects.common.util)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jgit)
            }
        }
    }

    val gradleApiVersions = project.gradleApiVersions()
    val kotlinToolingVersions = project.kotlinToolingVersions()
    targets.forEach { target ->
        val gradleApiVersion = gradleApiVersions.findCell(target.name) ?: return@forEach
        tasks.named(target.artifactsTaskName) {
            kotlinToolingVersions.cells.forEach { kotlinVersion ->
                val shimConfiguration = configurations.detachedConfiguration(
                    projects.kotlinGradlePluginShim.kotlinGradlePluginShimImpl,
                ).apply {
                    attributes {
                        attribute(
                            KotlinCompilerVersion.attribute,
                            objects.named(KotlinCompilerVersion::class.java, kotlinVersion.toString()),
                        )
                        attribute(
                            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                            objects.named(GradlePluginApiVersion::class.java, gradleApiVersion.gradleVersion.version),
                        )
                    }
                }
                dependsOn(shimConfiguration)
            }
        }
    }

//
//     targets.forEach { target ->
//         tasks.named(target.artifactsTaskName) {
//             project.kotlinToolingVersions().cells.forEach { kotlinVersion ->
//                 val shimConfiguration = configurations.detachedConfiguration(
//                     projects.kotlinGradlePluginShim.kotlinGradlePluginShimImpl,
//                 ).apply {
//                     attributes {
//                         attribute(
//                             KotlinCompilerVersion.attribute,
//                             objects.named(KotlinCompilerVersion::class.java, kotlinVersion.toString()),
//                         )
//                         attribute(
//                             GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
//                             objects.named(GradlePluginApiVersion::class.java, target.name),
//                         )
//                     }
//                 }
//                 dependsOn(shimConfiguration)
//             }
//         }
//     }
}

// fun DependencyHandlerScope.testableCompileOnly(dependencyNotation: Any) {
//     compileOnly(dependencyNotation)
//     pluginImplementationForTests(dependencyNotation)
// }

// dependencies {
//     testableCompileOnly(gradleApi())
//     testableCompileOnly(gradleKotlinDsl())
//     testableCompileOnly(libs.plugin.kotlin.gradle)
//     testableCompileOnly(libs.plugin.kotlin.gradle.api)
//
//     implementation(libs.kotlinx.serialization.json)
//     implementation(libs.jgit)
//     implementation(projects.common.analytics)
//     implementation(projects.common.configuration)
//     implementation(projects.common.license)
//     implementation(projects.common.util)
//
//     testImplementation(gradleApi())
//     testImplementation(gradleKotlinDsl())
//     testImplementation(gradleTestKit())
//     testImplementation(libs.plugin.kotlin.gradle)
//     testImplementation(libs.plugin.kotlin.gradle.api)
// }

// tasks.withType<PluginUnderTestMetadata>().configureEach {
//     pluginClasspath.from(pluginImplementationForTests)
// }

// tasks.named<Test>("test").configure {
//     systemProperty("testTmpDir", layout.buildDirectory.dir("external-libraries-tests").get().asFile.absolutePath)
//
//     dependsOn(rootProject.subprojects.mapNotNull {
//         if (it.name == "gradle-plugin" || it.tasks.findByName("publishToMavenLocal") == null) {
//             null
//         } else {
//             it.tasks.named("publishToMavenLocal")
//         }
//     })
// }

configurations.configureEach {
    attributes {
        @Suppress("UnstableApiUsage")
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("7.3"))
    }
}

gradlePlugin {
    this.plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SkieLoaderPlugin"
            version = project.version
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
// pluginBundle {
//     website = "https://github.com/touchlab/SKIE"
//     vcsUrl = "https://github.com/touchlab/SKIE.git"
//     description = "A Gradle plugin to add Swift into Kotlin/Native framework."
//     tags = listOf(
//         "plugin",
//         "gradle",
//         "swift",
//         "kotlin",
//         "native",
//     )
// }
