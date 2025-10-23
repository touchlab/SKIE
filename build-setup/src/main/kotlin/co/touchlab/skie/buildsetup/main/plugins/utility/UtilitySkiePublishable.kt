package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.main.extensions.SkiePublishingExtension
import co.touchlab.skie.buildsetup.util.mavenArtifactId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class UtilitySkiePublishable : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<MavenPublishPlugin>()

        val extension = extensions.create<SkiePublishingExtension>("skiePublishing")

        configureSmokeTestTmpRepository()
        configureSigningIfNeeded()
        configureMetadata(extension)
        configureKotlinJvmPublicationIfNeeded()
    }

    private fun Project.configureSmokeTestTmpRepository() {
        val smokeTestTmpRepositoryPath: String? by this

        smokeTestTmpRepositoryPath?.let {
            extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        url = uri(it)
                        name = "smokeTestTmp"
                    }
                }
            }
        }
    }

    private fun Project.configureSigningIfNeeded() {
        val isRelease = !version.toString().endsWith("SNAPSHOT")
        val isPublishing = gradle.startParameter.taskNames.contains("publishToSonatype")

        val shouldSign = isRelease && isPublishing
        if (shouldSign) {
            apply<SigningPlugin>()

            val signingExtension = extensions.getByType<SigningExtension>()

            val signingKey: String? by project
            val signingPassword: String? by project

            if (!signingKey.isNullOrBlank()) {
                signingExtension.useInMemoryPgpKeys(signingKey, signingPassword)
            }

            extensions.configure<PublishingExtension> {
                publications.withType<MavenPublication>().configureEach {
                    signingExtension.sign(this)
                }
            }
        }
    }

    private fun Project.configureMetadata(extension: SkiePublishingExtension) {
        gradle.taskGraph.whenReady {
            requireNotNull(extension.name.orNull) { "Module name not set for project ${name}!" }
            requireNotNull(extension.description.orNull) { "Module description not set for project ${name}!" }
        }

        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.assign(extension.name)
                    description = extension.description
                    url = "https://skie.touchlab.co"

                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }

                    developers {
                        listOf(
                            "Kevin Galligan" to "kevin@touchlab.co",
                            "Filip Dolnik" to "filip@touchlab.co",
                            "Tadeas Kriz" to "tadeas@touchlab.co",
                        ).forEach { (name, email) ->
                            developer {
                                this.name = name
                                this.email = email
                                organization = "Touchlab"
                                organizationUrl = "https://touchlab.co"
                            }
                        }
                    }

                    scm {
                        connection = "scm:git:git://github.com/touchlab/SKIE.git"
                        developerConnection = "scm:git:ssh://github.com:touchlab/SKIE.git"
                        url = "https://github.com/touchlab/SKIE"
                    }
                }
            }
        }
    }

    private fun Project.configureKotlinJvmPublicationIfNeeded() {
        plugins.withType<KotlinPluginWrapper>().configureEach {
            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
                withJavadocJar()
            }

            extensions.configure<PublishingExtension> {
                publications.create<MavenPublication>("maven") {
                    artifactId = mavenArtifactId

                    from(components.getAt("java"))
                }
            }
        }
    }
}
