package co.touchlab.skie.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.maven

abstract class BaseSkiePublishPlugin : Plugin<Project> {

    private val accessKeyProvider = EnvironmentVariableProvider("AWS_TOUCHLAB_DEPLOY_ACCESS")
    private val secretKeyProvider = EnvironmentVariableProvider("AWS_TOUCHLAB_DEPLOY_SECRET")

    override fun apply(target: Project) {
        applyPublishingPlugin(target)
        configurePublishingRepository(target)
        verifyKeysArePresent(target)
    }

    private fun applyPublishingPlugin(target: Project) {
        target.plugins.apply(MavenPublishPlugin::class.java)
    }

    private fun configurePublishingRepository(target: Project) {
        target.extensions.configure(PublishingExtension::class.java) {
            repositories {
                maven(target.awsUrl) {
                    name = "aws"

                    credentials(AwsCredentials::class) {
                        accessKey = accessKeyProvider.valueOrEmpty
                        secretKey = secretKeyProvider.valueOrEmpty
                    }
                }
            }
        }
    }

    private val Project.awsUrl: String
        get() {
            val isReleaseBuild = !this.version.toString().contains("-SNAPSHOT")

            return if (isReleaseBuild) "s3://touchlab-repo/release" else "s3://touchlab-repo/snapshot"
        }

    private fun verifyKeysArePresent(target: Project) {
        target.gradle.taskGraph.whenReady {
            val willPublish = this.allTasks.any { it is PublishToMavenRepository }

            if (willPublish) {
                accessKeyProvider.verifyWasSet()
                secretKeyProvider.verifyWasSet()
            }
        }
    }

    private class EnvironmentVariableProvider(private val name: String) {

        private val value: String? = System.getenv(name)

        val valueOrEmpty: String = value ?: ""

        fun verifyWasSet() {
            if (value == null) {
                throw IllegalStateException("Missing environment variable \"$name\"")
            }
        }
    }
}
