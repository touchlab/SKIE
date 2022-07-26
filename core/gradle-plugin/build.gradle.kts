plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"swiftgen-sealed\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project(":compiler-plugin").name}\"")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(project(":compiler-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create("swiftgen-sealed") {
            id = "co.touchlab.swiftgen.sealed"
            displayName = "SwiftGen-Sealed plugin"
            implementationClass = "co.touchlab.swiftgen.sealed.gradle.SwiftGenSealedSubplugin"
            version = project.version
        }
    }
}
