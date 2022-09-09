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
        the<JavaPluginExtension>().apply {
            withJavadocJar()
            withSourcesJar()
        }

        the<PublishingExtension>().apply {
            if (this@subprojects.name != "swiftpack-gradle-plugin") {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                    }
                }
            }

            repositories {
                val isReleaseBuild = !version.toString().contains("-SNAPSHOT")
                val awsUrl = if (isReleaseBuild) {
                    "s3://touchlab-repo/release"
                } else {
                    "s3://touchlab-repo/snapshot"
                }
                maven(awsUrl) {
                    name = "aws"

                    val awsAccessKey = System.getenv("AWS_TOUCHLAB_DEPLOY_ACCESS") ?: run {
                        logger.warn("AWS_TOUCHLAB_DEPLOY_ACCESS not set")
                        return@maven
                    }
                    val awsSecretKey = System.getenv("AWS_TOUCHLAB_DEPLOY_SECRET") ?: run {
                        logger.warn("AWS_TOUCHLAB_DEPLOY_SECRET not set")
                        return@maven
                    }
                    credentials(AwsCredentials::class) {
                        accessKey = awsAccessKey
                        secretKey = awsSecretKey
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
