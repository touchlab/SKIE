plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

group = "co.touchlab.skie"

sourceSets {
    main {
        kotlin.srcDir("src/main/kotlin-tooling-version")
    }
}

dependencies {
    implementation(libs.plugin.kotlin.gradle)
    implementation(libs.plugin.kotlin.gradle.api)
}

gradlePlugin {
    plugins.register("base.root") {
        id = "base.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BaseRoot"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
