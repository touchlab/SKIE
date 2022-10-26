plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    `maven-publish`
    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(("${project.group}.${project.name}").replace("-", "_"))
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project(":kotlin-plugin").name}\"")

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    implementation("io.outfoxx:swiftpoet:1.4.3")
    implementation("com.squareup:kotlinpoet:1.11.0")

    implementation(projects.configurationApi)
    implementation(projects.kotlinPlugin)
    implementation(projects.kotlinPlugin.options)

    testImplementation(libs.bundles.testing.jvm)
    testImplementation(gradleKotlinDsl())
    testImplementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    //
    // mavenCoordinates {
    //     groupId = PluginCoordinates.GROUP
    //     artifactId = PluginCoordinates.ARTIFACT_ID
    //     version = PluginCoordinates.VERSION
    // }
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}
