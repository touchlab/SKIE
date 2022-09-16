plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    `maven-publish`

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"co.touchlab.swiftpack\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${projects.swiftpackConfigPlugin.name}\"")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    implementation(libs.swiftPoet)
    implementation(projects.swiftpackSpi)
    api(projects.swiftpackPluginApi)
    implementation(projects.swiftpackConfigPlugin)

    testImplementation(libs.junit)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create("swiftpack") {
            id = "co.touchlab.swiftpack"
            displayName = "SwiftPack Plugin"
            implementationClass = "co.touchlab.swiftpack.plugin.SwiftPackPlugin"
            version = project.version
        }
    }
}

// // Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = "https://github.com/touchlab/SwiftPack"
    vcsUrl = "https://github.com/touchlab/SwiftPack.git"
    description = "A Gradle plugin to publish Swift templates into Maven repositories"
    tags = listOf("plugin", "gradle", "swift", "template", "kotlin", "native")
}
