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
    plugins.register("dev.settings") {
        id = "dev.settings"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevSettings"
    }
}
