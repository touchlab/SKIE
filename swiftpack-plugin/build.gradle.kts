import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.versionCheck)
}

allprojects {
    group = "co.touchlab.swiftpack"
    version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }

    apply {
        // plugin("io.gitlab.arturbosch.detekt")
        // plugin("org.jlleitschuh.gradle.ktlint")
    }

    // ktlint {
    //     debug.set(false)
    //     verbose.set(true)
    //     android.set(false)
    //     outputToConsole.set(true)
    //     ignoreFailures.set(false)
    //     enableExperimentalRules.set(true)
    //     filter {
    //         exclude("**/generated/**")
    //         include("**/kotlin/**")
    //     }
    // }
    //
    // detekt {
    //     config = rootProject.files("../config/detekt/detekt.yml")
    // }
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation.set(file("build/reports/detekt.html"))
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
