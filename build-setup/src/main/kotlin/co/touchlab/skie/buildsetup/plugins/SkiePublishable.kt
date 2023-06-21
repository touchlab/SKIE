package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.EnvironmentVariableProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.maven

class SkiePublishable: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<MavenPublishPlugin>()

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
    }
}
