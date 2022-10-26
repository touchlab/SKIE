plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.versionCheck)

    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.skie"
    version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
        maven("https://api.touchlab.dev/public")
    }
}

subprojects {
    afterEvaluate {
        if (!plugins.hasPlugin(PublishingPlugin::class)) {
            return@afterEvaluate
        }

        if (this@subprojects.name != "configuration-annotations") {
            the<JavaPluginExtension>().apply {
                withJavadocJar()
                withSourcesJar()
            }
        }

        the<PublishingExtension>().apply {
            if (this@subprojects.name != "gradle-plugin" && this@subprojects.name != "configuration-annotations") {
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

val cleanRoot by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}

//tasks.withType<DependencyUpdatesTask> {
//    rejectVersionIf {
//        candidate.version.isNonStable()
//    }
//}
//
//fun String.isNonStable() = "^[0-9,.v-]+(-r)?$".toRegex().matches(this).not()

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("cleanAll") {
    dependsOn(cleanRoot)
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
