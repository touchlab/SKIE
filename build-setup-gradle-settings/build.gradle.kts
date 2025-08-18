plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}

gradlePlugin {
    plugins.register("dev.gradle.settings") {
        id = "dev.gradle.settings"
        implementationClass = "co.touchlab.skie.buildsetup.settings.plugins.DevGradleSettings"
    }
}
