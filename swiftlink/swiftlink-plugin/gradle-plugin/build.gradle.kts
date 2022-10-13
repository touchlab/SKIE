plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    `maven-publish`
    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project(":plugin").name}\"")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    implementation("io.outfoxx:swiftpoet:1.4.0")
    implementation("com.squareup:kotlinpoet:1.11.0")

    implementation(project(":plugin"))
    implementation(libs.swiftpack.gradle.plugin)
    implementation(libs.swiftpack.config.plugin)
    implementation(libs.swiftpack.spi)

    testImplementation(gradleKotlinDsl())
    testImplementation(libs.junit)
    testImplementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create(PluginCoordinates.ID) {
            id = PluginCoordinates.ID
            displayName = PluginBundle.DISPLAY_NAME
            implementationClass = PluginCoordinates.IMPLEMENTATION_CLASS
            version = PluginCoordinates.VERSION
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = PluginBundle.WEBSITE
    vcsUrl = PluginBundle.VCS
    description = PluginBundle.DESCRIPTION
    tags = PluginBundle.TAGS

    mavenCoordinates {
        groupId = PluginCoordinates.GROUP
        artifactId = PluginCoordinates.ARTIFACT_ID
        version = PluginCoordinates.VERSION
    }
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
