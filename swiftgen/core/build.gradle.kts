plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.swiftgen"
    version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"
}

subprojects {
    afterEvaluate {
        the<PublishingExtension>().apply {
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

tasks.create("cleanAll") {
    allprojects.forEach { project ->
        project.afterEvaluate {
            project.tasks.findByName("clean")?.let {
                dependsOn(it)
            }
        }
    }
}
