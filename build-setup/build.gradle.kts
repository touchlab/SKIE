plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "co.touchlab.skie"

dependencies {
    api(projects.shared)

    implementation(libs.plugin.kotlin.gradle.plugin)
    implementation(libs.plugin.kotlin.gradle.plugin.api)
    implementation(libs.plugin.kotlin.sam.with.receiver)
    implementation(libs.plugin.kotlin.serialization)

    implementation(libs.plugin.pluginPublish)
    implementation(libs.plugin.shadow)
    implementation(libs.plugin.buildconfig)
    implementation(libs.plugin.mavenPublish)
}

gradlePlugin {
    plugins.register("base.kotlin") {
        id = "base.kotlin"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin"
    }

    plugins.register("base.tests") {
        id = "base.tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.base.BaseTestsPlugin"
    }

    plugins.register("tests.functional-tests") {
        id = "tests.functional-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsFunctionalTestsPlugin"
    }

    plugins.register("tests.type-mapping-tests") {
        id = "tests.type-mapping-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsTypeMappingTestsPlugin"
    }

    plugins.register("tests.library-tests") {
        id = "tests.library-tests"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsLibraryTestsPlugin"
    }

    plugins.register("tests.dependencies") {
        id = "tests.dependencies"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsDependenciesPlugin"
    }

    plugins.register("tests.acceptance-tests-framework") {
        id = "tests.acceptance-tests-framework"
        implementationClass = "co.touchlab.skie.buildsetup.tests.plugins.tests.TestsAcceptanceTestsFrameworkPlugin"
    }

    plugins.register("utility.build-config") {
        id = "utility.build-config"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfigPlugin"
    }

    plugins.register("utility.experimental.context-receivers") {
        id = "utility.experimental.context-receivers"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityExperimentalContextReceiversPlugin"
    }

    plugins.register("utility.gradle.implicit-receiver") {
        id = "utility.gradle.implicit-receiver"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiverPlugin"
    }

    plugins.register("utility.gradle.minimum-target-kotlin-version") {
        id = "utility.gradle.minimum-target-kotlin-version"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersionPlugin"
    }

    plugins.register("utility.minimum-target-kotlin-version") {
        id = "utility.minimum-target-kotlin-version"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin"
    }

    plugins.register("utility.multi-kotlin-version-support") {
        id = "utility.multi-kotlin-version-support"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupportPlugin"
    }

    plugins.register("utility.opt-in.experimental-compiler-api") {
        id = "utility.opt-in.experimental-compiler-api"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApiPlugin"
    }

    plugins.register("utility.skie-publishable") {
        id = "utility.skie-publishable"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.utility.UtilitySkiePublishablePlugin"
    }

    plugins.register("skie.root") {
        id = "skie.root"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRootPlugin"
    }

    plugins.register("skie.common") {
        id = "skie.common"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCommonPlugin"
    }

    plugins.register("skie.compiler.linker") {
        id = "skie.compiler.linker"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCompilerLinkerPlugin"
    }

    plugins.register("skie.compiler.core") {
        id = "skie.compiler.core"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieCompilerCorePlugin"
    }

    plugins.register("skie.configuration-annotations") {
        id = "skie.configuration-annotations"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieConfigurationAnnotationsPlugin"
    }

    plugins.register("skie.runtime.kotlin") {
        id = "skie.runtime.kotlin"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRuntimeKotlinPlugin"
    }

    plugins.register("skie.runtime.swift") {
        id = "skie.runtime.swift"
        implementationClass = "co.touchlab.skie.buildsetup.main.plugins.skie.SkieRuntimeSwiftPlugin"
    }

    plugins.register("gradle.common") {
        id = "gradle.common"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradleCommonPlugin"
    }

    plugins.register("gradle.plugin") {
        id = "gradle.plugin"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradlePluginPlugin"
    }

    plugins.register("gradle.shim") {
        id = "gradle.shim"
        implementationClass = "co.touchlab.skie.buildsetup.gradle.plugins.gradle.GradleShimPlugin"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
