import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.versionCheck)
}

allprojects {
    group = PluginCoordinates.GROUP
    version = PluginCoordinates.VERSION

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
            name = "gitHub-swiftpack"
            credentials {
                val githubActor: String? by project
                val githubToken: String? by project
                username = System.getenv("TL_READ_ACTOR") ?: githubActor
                password = System.getenv("TL_READ_TOKEN") ?: githubToken
            }
        }
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
