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
        kotlin.srcDir("src/main/kotlin-shared-build-setup")
    }
}

dependencies {
    implementation(libs.plugin.kotlin.gradle)
    implementation(libs.plugin.kotlin.gradle.api)
    implementation(libs.plugin.kotlin.samWithReceiver)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.pluginPublish)
    implementation(libs.plugin.buildconfig)
    implementation(libs.kotlinPoet)
}

gradlePlugin {
    plugins.register("base.root") {
        id = "base.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BaseRoot"
    }

    plugins.register("base.kotlin") {
        id = "base.kotlin"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin"
    }

    plugins.register("dev.root") {
        id = "dev.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.dev.DevRoot"
    }

    plugins.register("dev.multiplatform") {
        id = "dev.multiplatform"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.dev.DevMultiplatform"
    }

    plugins.register("dev.acceptance-tests") {
        id = "dev.acceptance-tests"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevAcceptanceTests"
    }

    plugins.register("dev.acceptance-tests-framework") {
        id = "dev.acceptance-tests-framework"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.DevAcceptanceTestsFramework"
    }

    plugins.register("utility.build-config") {
        id = "utility.build-config"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfig"
    }

    plugins.register("utility.experimental.context-receivers") {
        id = "utility.experimental.context-receivers"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityExperimentalContextReceivers"
    }

    plugins.register("utility.gradle-implicit-receiver") {
        id = "utility.gradle-implicit-receiver"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityGradleImplicitReceiver"
    }

    plugins.register("utility.opt-in.experimental-compiler-api") {
        id = "utility.opt-in.experimental-compiler-api"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApi"
    }

    plugins.register("skie.root") {
        id = "skie.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRoot"
    }

    plugins.register("skie.common") {
        id = "skie.common"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCommon"
    }

    plugins.register("skie.compiler") {
        id = "skie.compiler"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieCompiler"
    }

    plugins.register("skie.compiler.core") {
        id = "skie.compiler.core"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieCompilerCore"
    }

    plugins.register("skie.publishable") {
        id = "skie.publishable"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkiePublishable"
    }

    plugins.register("skie.multicompile") {
        id = "skie.multicompile"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieMultiCompileRuntime"
    }

    plugins.register("skie.configuration-annotations") {
        id = "skie.configuration-annotations"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieConfigurationAnnotations"
    }

    plugins.register("skie.runtime.kotlin") {
        id = "skie.runtime.kotlin"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieRuntimeKotlin"
    }

    plugins.register("skie.runtime.swift") {
        id = "skie.runtime.swift"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRuntimeSwift"
    }

    plugins.register("skie.shim") {
        id = "skie.shim"
        implementationClass = "co.touchlab.skie.buildsetup.plugins.SkieShim"
    }

    plugins.register("skie.gradle") {
        id = "skie.gradle"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.gradle.SkieGradle"
    }

    plugins.register("skie.gradle.plugin") {
        id = "skie.gradle.plugin"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.gradle.SkieGradlePlugin"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
