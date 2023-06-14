import co.touchlab.skie.gradle.util.EnvironmentVariableProvider

plugins {
    `maven-publish`
}

version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }

// Configure publishing to AWS S3
val accessKeyProvider = EnvironmentVariableProvider("AWS_TOUCHLAB_DEPLOY_ACCESS")
val secretKeyProvider = EnvironmentVariableProvider("AWS_TOUCHLAB_DEPLOY_SECRET")

extensions.configure<PublishingExtension> {
    repositories {
        val isReleaseBuild = !version.toString().contains("-SNAPSHOT")
        val awsUrl = if (isReleaseBuild) "s3://touchlab-repo/release" else "s3://touchlab-repo/snapshot"
        maven(awsUrl) {
            name = "aws"

            credentials(AwsCredentials::class) {
                accessKey = accessKeyProvider.valueOrEmpty
                secretKey = secretKeyProvider.valueOrEmpty
            }
        }
    }
}

// Verify keys are present when publishing
gradle.taskGraph.whenReady {
    val willPublish = allTasks.any { it is PublishToMavenRepository }

    if (willPublish) {
        accessKeyProvider.verifyWasSet()
        secretKeyProvider.verifyWasSet()
    }
}

// Disable sources publishing for Kotlin Multiplatform modules
tasks.matching {
    it.name.endsWith("sourcesJar", ignoreCase = true)
}.all {
    enabled = false
}
