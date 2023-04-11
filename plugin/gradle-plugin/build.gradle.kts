import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.publish.mavenArtifactId

plugins {
    id("skie-jvm")
    id("java-gradle-plugin")
    id("skie-publish-gradle")
    id("skie-buildconfig")
}

buildConfig {
    val kotlinPlugin = projects.kotlinPlugin.dependencyProject
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${kotlinPlugin.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${kotlinPlugin.mavenArtifactId}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${kotlinPlugin.version}\"")

    val runtime = projects.runtime.kotlin.dependencyProject
    buildConfigField("String", "RUNTIME_DEPENDENCY", "\"${runtime.dependencyName}\"")

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}

val pluginImplementationForTests = configurations.create("pluginImplementationForTests")

fun DependencyHandlerScope.testableCompileOnly(dependencyNotation: Any) {
    compileOnly(dependencyNotation)
    pluginImplementationForTests(dependencyNotation)
}

dependencies {
    testableCompileOnly(gradleApi())
    testableCompileOnly(gradleKotlinDsl())
    testableCompileOnly(libs.plugin.kotlin.gradle)
    testableCompileOnly(libs.plugin.kotlin.gradle.api)

    implementation(libs.kotlinx.serialization.json)
    implementation(projects.configurationApi)
    implementation(projects.analytics.analyticsApi)
    implementation(projects.analytics.analyticsConfiguration)
    implementation(projects.analytics.producer)
    implementation(projects.generator.configurationGradle)
    implementation(projects.kotlinPlugin.options)
    implementation(projects.util)

    testImplementation(gradleApi())
    testImplementation(gradleKotlinDsl())
    testImplementation(gradleTestKit())
    testImplementation(libs.plugin.kotlin.gradle)
    testImplementation(libs.plugin.kotlin.gradle.api)
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(pluginImplementationForTests.asPath)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<Test>("test").configure {
    systemProperty("testTmpDir", layout.buildDirectory.dir("external-libraries-tests").get().asFile.absolutePath)

    dependsOn(rootProject.subprojects.mapNotNull {
        if (it.name == "gradle-plugin" || it.tasks.findByName("publishToMavenLocal") == null) {
            null
        } else {
            it.tasks.named("publishToMavenLocal")
        }
    })
}

configurations.configureEach {
    attributes {
        @Suppress("UnstableApiUsage")
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("7.3"))
    }
}

gradlePlugin {
    plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SwiftLinkPlugin"
            version = project.version
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = "https://github.com/touchlab/SKIE"
    vcsUrl = "https://github.com/touchlab/SKIE.git"
    description = "A Gradle plugin to add Swift into Kotlin/Native framework."
    tags = listOf(
        "plugin",
        "gradle",
        "swift",
        "kotlin",
        "native",
    )
}
