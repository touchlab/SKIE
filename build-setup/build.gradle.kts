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
        kotlin.srcDir("src/main/kotlin-tooling-version")
        kotlin.srcDir("src/main/kotlin-compiler-attribute-build-setup")
    }
}

dependencies {
    val usedCompilerVersion = project.property("versionSupport.kotlin.usedCompiler.version").toString()

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$usedCompilerVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$usedCompilerVersion")
    implementation("org.jetbrains.kotlin:kotlin-sam-with-receiver:$usedCompilerVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$usedCompilerVersion")

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

    plugins.register("base.tests") {
        id = "base.tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.base.BaseTests"
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

    plugins.register("tests.functional-tests") {
        id = "tests.functional-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsFunctionalTests"
    }

    plugins.register("tests.stdlib-tests") {
        id = "tests.stdlib-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsStdlibTests"
    }

    plugins.register("tests.type-mapping-tests") {
        id = "tests.type-mapping-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsTypeMappingTests"
    }

    plugins.register("tests.dependencies") {
        id = "tests.dependencies"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsDependencies"
    }

    plugins.register("tests.acceptance-tests-framework") {
        id = "tests.acceptance-tests-framework"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsAcceptanceTestsFramework"
    }

    plugins.register("utility.build-config") {
        id = "utility.build-config"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfig"
    }

    plugins.register("utility.experimental.context-receivers") {
        id = "utility.experimental.context-receivers"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityExperimentalContextReceivers"
    }

    plugins.register("utility.gradle.implicit-receiver") {
        id = "utility.gradle.implicit-receiver"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiver"
    }

    plugins.register("utility.gradle.minimum-target-kotlin-version") {
        id = "utility.gradle.minimum-target-kotlin-version"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersion"
    }

    plugins.register("utility.minimum-target-kotlin-version") {
        id = "utility.minimum-target-kotlin-version"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion"
    }

    plugins.register("utility.multi-kotlin-version-support") {
        id = "utility.multi-kotlin-version-support"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupport"
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

    plugins.register("skie.compiler.linker") {
        id = "skie.compiler.linker"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCompilerLinker"
    }

    plugins.register("skie.compiler.core") {
        id = "skie.compiler.core"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCompilerCore"
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
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRuntimeKotlin"
    }

    plugins.register("skie.runtime.swift") {
        id = "skie.runtime.swift"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRuntimeSwift"
    }

    plugins.register("gradle.common") {
        id = "gradle.common"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradleCommon"
    }

    plugins.register("gradle.plugin") {
        id = "gradle.plugin"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradlePlugin"
    }

    plugins.register("gradle.shim") {
        id = "gradle.shim"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradleShim"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
