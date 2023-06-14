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
        kotlin.srcDir("src/main/kotlin-compiler-attribute-local")
    }
}

dependencies {
    implementation(libs.plugin.kotlin.gradle)
    implementation(libs.plugin.kotlin.gradle.api)
    implementation(libs.plugin.kotlin.samWithReceiver)
    implementation(libs.plugin.pluginPublish)
    implementation(libs.plugin.buildconfig)
    implementation(libs.ktor.client.java)
}

gradlePlugin {
    plugins.register("buildconfig") {
        id = "skie-buildconfig"
        implementationClass = "co.touchlab.skie.gradle.buildconfig.SkieBuildConfigPlugin"
    }
    plugins.register("debug") {
        id = "skie-debug"
        implementationClass = "co.touchlab.skie.gradle.debug.GradleScriptDebugPlugin"
    }
    plugins.register("jvm") {
        id = "skie-jvm"
        implementationClass = "co.touchlab.skie.gradle.kotlin.SkieKotlinJvmPlugin"
    }
    plugins.register("multiplatform") {
        id = "skie-multiplatform"
        implementationClass = "co.touchlab.skie.gradle.kotlin.SkieKotlinMultiplatformPlugin"
    }
    plugins.register("publish-jvm") {
        id = "skie-publish-jvm"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishJvmPlugin"
    }
    plugins.register("publish-multiplatform") {
        id = "skie-publish-multiplatform"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishMultiplatformPlugin"
    }
    plugins.register("publish-gradle") {
        id = "skie-publish-gradle"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishGradlePlugin"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
