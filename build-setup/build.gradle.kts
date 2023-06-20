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


    plugins.register("dev.buildconfig") {
        id = "dev.buildconfig"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevBuildconfig"
    }

    plugins.register("dev.jvm") {
        id = "dev.jvm"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevJvm"
    }

    plugins.register("dev.multiplatform") {
        id = "dev.multiplatform"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevMultiplatform"
    }

    plugins.register("dev.root") {
        id = "dev.root"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevRoot"
    }

    plugins.register("experimental.context-receivers") {
        id = "experimental.context-receivers"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.ExperimentalContextReceivers"
    }

    plugins.register("skie.common") {
        id = "skie.common"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieCommon"
    }

    plugins.register("skie.compiler") {
        id = "skie.compiler"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieCompiler"
    }

    plugins.register("skie.gradle") {
        id = "skie.gradle"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieGradle"
    }

    plugins.register("skie.publishable") {
        id = "skie.publishable"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkiePublishable"
    }

    plugins.register("skie.runtime") {
        id = "skie.runtime"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieRuntime"
    }

    plugins.register("skie.runtime.kotlin") {
        id = "skie.runtime.kotlin"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieRuntimeKotlin"
    }

    plugins.register("skie.runtime.swift") {
        id = "skie.runtime.swift"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieRuntimeSwift"
    }

    plugins.register("skie.server") {
        id = "skie.server"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieServer"
    }

    plugins.register("skie.shim") {
        id = "skie.shim"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieShim"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
