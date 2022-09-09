import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
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
        maven("https://api.touchlab.dev/public")
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

subprojects {
    afterEvaluate {
        if (!plugins.hasPlugin(PublishingPlugin::class)) { return@afterEvaluate }

        the<JavaPluginExtension>().apply {
            withJavadocJar()
            withSourcesJar()
        }

        the<PublishingExtension>().apply {
            if (this@subprojects.name != "gradle-plugin") {
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
