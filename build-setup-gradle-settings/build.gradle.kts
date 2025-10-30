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
    plugins.register("settings.gradle") {
        id = "settings.gradle"
        implementationClass = "co.touchlab.skie.buildsetup.settings.plugins.SettingsGradlePlugin"
    }
}
