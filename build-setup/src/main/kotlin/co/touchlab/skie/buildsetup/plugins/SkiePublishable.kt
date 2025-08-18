package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.plugins.extensions.HasMavenPublishPlugin
import co.touchlab.skie.buildsetup.plugins.extensions.HasSigningPlugin
import co.touchlab.skie.buildsetup.plugins.util.SkiePublishingExtension
import co.touchlab.skie.gradle.publish.mavenArtifactId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkiePublishable : Plugin<Project>, HasMavenPublishPlugin, HasSigningPlugin {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<MavenPublishPlugin>()
        configureSigningIfNeeded()

        val extension = extensions.create<SkiePublishingExtension>("skiePublishing")

        configureSmokeTestTmpRepository()
        configureMetadata(extension)
        configureKotlinJvmPublicationIfNeeded()
        configureSourcesJar(extension)
        configureJavadocJar(extension)
    }

    private fun Project.configureSmokeTestTmpRepository() {
        val smokeTestTmpRepositoryPath: String? by this
        smokeTestTmpRepositoryPath?.let {
            publishing {
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
        println("Configuring signing for project ${name} (isRelease: $isRelease, isPublishing: $isPublishing)")
        val shouldSign = isRelease && isPublishing
        if (shouldSign) {
            apply<SigningPlugin>()
            val signingKey: String? by project
            val signingPassword: String? by project
            if (!signingKey.isNullOrBlank()) {
                signing.useInMemoryPgpKeys(signingKey, signingPassword)
            }
            publishing.publications.withType<MavenPublication>().configureEach {
                signing.sign(this)
            }
        }
    }

    private fun Project.configureMetadata(extension: SkiePublishingExtension) {
        gradle.taskGraph.whenReady {
            requireNotNull(extension.name.orNull) { "Module name not set for project ${name}!" }
            requireNotNull(extension.description.orNull) { "Module description not set for project ${name}!" }
        }

        publishing {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name = extension.name
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

    private fun Project.configureKotlinJvmPublicationIfNeeded() = afterEvaluate {
        if (plugins.hasPlugin(KotlinPluginWrapper::class.java) && !plugins.hasPlugin(JavaGradlePluginPlugin::class.java)) {
            publishing.publications.create<MavenPublication>("maven") {
                artifactId = mavenArtifactId

                from(components.getAt("java"))
            }
        }
    }

    private fun Project.configureSourcesJar(extension: SkiePublishingExtension): Unit = afterEvaluate {
        // Gradle plugin already sets up Sources publishing
        if (plugins.hasPlugin(JavaGradlePluginPlugin::class.java)) {
            return@afterEvaluate
        }

        // Disable sources publishing for Kotlin Multiplatform modules
        if (!extension.publishSources.get()) {
            if (plugins.hasPlugin(KotlinMultiplatformPluginWrapper::class.java)) {
                tasks.matching {
                    it.name.endsWith("sourcesJar", ignoreCase = true)
                }.all {
                    if (this is Jar) {
                        exclude { true }
                    }
                }
            }
            if (plugins.hasPlugin(KotlinPluginWrapper::class.java)) {
                publishing.publications.withType<MavenPublication> {
                    val publication = this
                    val sourcesJar = tasks.register("${publication.name}SourcesJar", Jar::class) {
                        archiveClassifier.set("sources")
                        // Each archive name should be distinct.
                        archiveBaseName.set("${archiveBaseName.orNull ?: project.name}-${publication.name}")
                    }
                    artifact(sourcesJar)
                }
            }
        }
    }

    private fun Project.configureJavadocJar(extension: SkiePublishingExtension) = afterEvaluate {
        // Gradle plugin already sets up Javadoc publishing
        if (plugins.hasPlugin(JavaGradlePluginPlugin::class.java)) {
            return@afterEvaluate
        }

        if (!extension.publishJavadoc.get()) {
            publishing.publications.withType<MavenPublication> {
                val publication = this
                val javadocJar = tasks.register("${publication.name}JavadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    // Each archive name should be distinct. Mirror the format for the sources Jar tasks.
                    archiveBaseName.set("${archiveBaseName.orNull ?: project.name}-${publication.name}")
                }
                artifact(javadocJar)
            }
        }
    }
}
