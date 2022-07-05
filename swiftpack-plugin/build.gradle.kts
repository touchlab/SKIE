import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.publish.PublishingExtension

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
    apply(plugin = "maven-publish")

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

subprojects {
    afterEvaluate {
        the<PublishingExtension>().apply {
            if (this@subprojects.name != "swiftpack-gradle-plugin") {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                    }
                }
            }

            repositories {
                maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
                    name = "gitHub"

                    val actor = System.getenv("GITHUB_ACTOR") ?: run {
                        logger.warn("GITHUB_ACTOR not set")
                        return@maven
                    }
                    val password = System.getenv("GITHUB_TOKEN") ?: run {
                        logger.warn("GITHUB_TOKEN not set")
                        return@maven
                    }
                    credentials {
                        this.username = actor
                        this.password = password
                    }
                }
            }
        }
    }
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
