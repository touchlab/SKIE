import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.publish.mavenArtifactId
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

// import co.touchlab.skie.gradle.version.gradleApiVersions
// import co.touchlab.skie.gradle.version.kotlinToolingVersions

plugins {
    id("skie.gradle")
    id("skie.publishable")
    id("dev.buildconfig")

//     alias(libs.plugins.kotlin.plugin.serialization)
}

buildConfig {
    val gradlePlugin = projects.gradle.gradlePlugin.dependencyProject
    // TODO Rename to SKIE_GRADLE_PLUGIN
    buildConfigField("String", "SKIE_GRADLE_PLUGIN_DEPENDENCY", "\"${gradlePlugin.dependencyName}\"")
}

// val pluginImplementationForTests = configurations.create("pluginImplementationForTests")

kotlin {
    sourceSets {
        val commonMain by getting {
            kotlin.srcDirs(
                "src/gradle_common/kotlin-compiler-attribute",
            )

            dependencies {
                implementation(projects.gradle.gradlePluginApi)
                api(projects.common.configuration.configurationGradle)
            }
        }
    }

    val gradleApiVersions = project.gradleApiVersionDimension()
    val kotlinToolingVersions = project.kotlinToolingVersionDimension()
    targets.forEach { target ->
        val gradleApiVersion = gradleApiVersions.parse(target.name)?.components?.singleOrNull() ?: return@forEach
        tasks.named(target.artifactsTaskName) {
            kotlinToolingVersions.components.forEach { kotlinVersion ->
                val shimConfiguration = configurations.detachedConfiguration(
                    projects.gradle.gradlePlugin
                ).apply {
                    attributes {
                        attribute(
                            KotlinCompilerVersion.attribute,
                            objects.named(KotlinCompilerVersion::class.java, kotlinVersion.value),
                        )
                        attribute(
                            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                            objects.named(GradlePluginApiVersion::class.java, gradleApiVersion.value),
                        )
                    }
                }
                dependsOn(shimConfiguration)
            }
        }
    }
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
