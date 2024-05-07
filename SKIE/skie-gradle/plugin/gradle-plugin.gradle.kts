import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyCoordinate
import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle.plugin")
    id("skie.publishable")
    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin"
    description = "Gradle plugin for configuring SKIE compiler plugin."
}

buildConfig {
    val shimImpl = project.provider { projects.gradle.gradlePluginShimImpl.dependencyProject }
    buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", shimImpl.map { it.dependencyCoordinate.enquoted() })

    val kotlinToSkieKgpVersion = project.kotlinToolingVersionDimension().components
        .flatMap { versionComponent ->
            versionComponent.supportedVersions.map { version ->
                version to versionComponent.name
            }
        }
        .joinToString { (version, name) ->
            "${version.toString().enquoted()} to ${name.toString().enquoted()}"
        }

    buildConfigField("co.touchlab.skie.plugin.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "mapOf($kotlinToSkieKgpVersion)")

    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")

    val kotlinPlugin = project.provider { projects.kotlinCompiler.kotlinCompilerLinkerPlugin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })

    val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_COORDINATE", runtime.map { it.dependencyCoordinate.enquoted() })

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")

    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs(
                "src/main/kotlin-compiler-attribute",
            )
        }
    }
}

configurations.configureEach {
    attributes {
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleApiVersionDimension().components.min().value))
    }
}

dependencies {
    implementation(projects.gradle.gradlePluginShimApi)
    api(projects.common.configuration.configurationDeclaration)
    compileOnly("dev.gradleplugins:gradle-api:${gradleApiVersionDimension().components.min().value}")
    compileOnly(libs.plugin.kotlin.gradle.api)
    compileOnly(libs.plugin.kotlin.gradle)

    implementation(libs.ci.info)
    implementation(libs.jgit)
    implementation(libs.mixpanel)

    implementation(projects.common.analytics)
    implementation(projects.common.util)

    testImplementation(kotlin("test"))
}

tasks.named("compileKotlin").configure {
    val gradleApiVersions = project.gradleApiVersionDimension()
    val kotlinToolingVersions = project.kotlinToolingVersionDimension()

    gradleApiVersions.components.forEach { gradleApiVersion ->
        kotlinToolingVersions.components.forEach { kotlinToolingVersion ->
            val shimConfiguration = configurations.detachedConfiguration(
                projects.gradle.gradlePluginShimImpl,
            ).apply {
                attributes {
                    attribute(
                        KotlinCompilerVersion.attribute,
                        objects.named(KotlinCompilerVersion::class.java, kotlinToolingVersion.value),
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

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = "https://skie.touchlab.co"
    vcsUrl = "https://github.com/touchlab/SKIE.git"

    this.plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SkieGradlePlugin"
            version = project.version

            description = "A Gradle plugin to add Swift into Kotlin/Native framework."
            tags = listOf(
                "swift",
                "kotlin",
                "native",
                "compiler",
            )
        }
    }
}
