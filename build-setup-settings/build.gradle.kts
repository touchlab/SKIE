plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    main {
        kotlin.srcDir("src/main/kotlin-compiler-attribute-link")
        kotlin.srcDir("src/main/kotlin-shared-build-setup-link")
    }
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
