import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.gradlePluginApi
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("skie.gradle.plugin")
    id("skie.publishable")

    alias(libs.plugins.shadow)
}

tasks.shadowJar {
    archiveClassifier.set("")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib.*"))
    }
}

skiePublishing {
    name = "SKIE Gradle Plugin"
    description = "Gradle plugin for configuring SKIE compiler plugin."
}

configurations.configureEach {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(gradleApiVersionDimension().components.min().value),
        )
    }
}

kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
    val safeKotlinVersion = kotlinToolingVersion.value.replace('.', '_')
    val configuration = configurations.create("shim-relocation-kgp_$safeKotlinVersion") {
        attributes {
            attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
        }
    }

    val relocationTask = tasks.register<ShadowJar>("relocate-shim-kgp_$safeKotlinVersion") {
        relocate("co.touchlab.skie.plugin.shim.impl", "co.touchlab.skie.plugin.shim.impl_$safeKotlinVersion")
        configurations = listOf(configuration)
        archiveClassifier = "kgp_$safeKotlinVersion"
        dependencies {
//             exclude("**/*.kotlin_metadata")
//             exclude("**/*.kotlin_module")
//             exclude("**/*.kotlin_builtins")
        }
    }

    tasks.named("compileKotlin").configure {
        dependsOn(relocationTask)
    }

    dependencies {
        configuration(projects.gradle.gradlePluginShimImpl)
        runtimeOnly(relocationTask.map { it.outputs.files })
    }
}

dependencies {
    api(projects.gradle.gradlePluginApi)
    implementation(projects.gradle.gradlePluginImpl)

    compileOnly(gradlePluginApi())
    compileOnly(kotlin("stdlib"))
}

// tasks.named("compileKotlin").configure {
//     val gradleApiVersions = project.gradleApiVersionDimension()
//     val kotlinToolingVersions = project.kotlinToolingVersionDimension()
//
//     gradleApiVersions.components.forEach { gradleApiVersion ->
//         kotlinToolingVersions.components.forEach { kotlinToolingVersion ->
//             val shimConfiguration = configurations.detachedConfiguration(
//                 projects.gradle.gradlePluginShimImpl,
//             ).apply {
//                 attributes {
//                     attribute(
//                         KotlinCompilerVersion.attribute,
//                         objects.named(KotlinCompilerVersion::class.java, kotlinToolingVersion.value),
//                     )
//                     attribute(
//                         GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
//                         objects.named(GradlePluginApiVersion::class.java, gradleApiVersion.value),
//                     )
//                 }
//             }
//             dependsOn(shimConfiguration)
//         }
//     }
// }

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
