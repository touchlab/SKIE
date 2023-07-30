import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.gradleApiVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator

plugins {
    id("skie.shim")
    id("skie.publishable")

    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin"
    description = "Gradle plugin for configuring SKIE compiler plugin.."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // TODO: It might be worthwhile to make this compile-time safe, so we don't have to manually check. Or at least a test?
                // Whichever dependency is brought in by `gradle-plugin-loader` has to be `compileOnly` as we don't want duplicate classes.
                compileOnly(projects.gradle.gradlePluginApi)
                compileOnly(projects.common.configuration.configurationDeclaration)

                implementation(projects.common.analytics)
                implementation(projects.common.util)
            }
        }
    }
}

buildConfig {
    generator(
        ExpectActualBuildConfigGenerator(
            isActualImplementation = false,
            internalVisibility = false,
        )
    )

    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "")
    buildConfigField("String", "KOTLIN_TOOLING_VERSION", "")
    buildConfigField("String", "GRADLE_API_VERSION", "")
    buildConfigField("String", "RUNTIME_DEPENDENCY", "")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "")
}

multiDimensionTarget.configureSourceSet { sourceSet ->
    if (sourceSet.isRoot) {
        kotlinSourceSet.kotlin.srcDir(
            "src/kgp_common/gradle_common/kotlin-compiler-attribute-local",
        )
    }
}

multiDimensionTarget.configureSourceSet { sourceSet ->
    if (!sourceSet.isTarget || compilation.isTest) { return@configureSourceSet }

    buildConfig {
        this.sourceSets.named(kotlinSourceSet.name).configure {
            generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
            className.set("BuildConfig")

            val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject

            buildConfigField("String", "KOTLIN_PLUGIN_GROUP", kotlinPlugin.group.toString().enquoted())
            buildConfigField("String", "KOTLIN_PLUGIN_NAME", kotlinPlugin.name.enquoted())
            buildConfigField("String", "KOTLIN_PLUGIN_VERSION", kotlinPlugin.version.toString().enquoted())
            buildConfigField("String", "KOTLIN_TOOLING_VERSION", sourceSet.kotlinToolingVersion.value.enquoted())
            buildConfigField("String", "GRADLE_API_VERSION", sourceSet.gradleApiVersion.value.enquoted())

            val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject.dependencyName }
            buildConfigField("String", "RUNTIME_DEPENDENCY", runtime.map { it.enquoted() })

            val pluginId: String by properties
            buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
        }
    }
}
