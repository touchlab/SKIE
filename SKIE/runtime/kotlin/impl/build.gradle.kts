import io.github.gradlenexus.publishplugin.InitializeNexusStagingRepository
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform") version "@targetKotlinVersion@"
    `maven-publish`
    alias(libs.plugins.nexusPublish)
}

group = "co.touchlab.skie"
version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }

nexusPublishing {
    repositoryDescription = "$group:SKIE:$version"

    this.repositories {
        sonatype()
    }
}

tasks.withType<InitializeNexusStagingRepository>().configureEach {
    isEnabled = false
}

@smokeTestTmpRepositoryConfiguration@

val isRelease = !version.toString().endsWith("SNAPSHOT")
val isPublishing = gradle.startParameter.taskNames.contains("publishToSonatype")
val shouldSign = isRelease && isPublishing
if (shouldSign) {
    apply<SigningPlugin>()
    val signing = extensions.getByType<SigningExtension>()
    val signingKey: String? by project
    val signingPassword: String? by project
    if (!signingKey.isNullOrBlank()) {
        signing.useInMemoryPgpKeys(signingKey, signingPassword)
    }
    publishing.publications.withType<MavenPublication>().configureEach {
        signing.sign(this)
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "SKIE Runtime - Kotlin"
            description = "Kotlin Multiplatform part of the SKIE runtime. It's used to facilitate certain features of SKIE."
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

        val publication = this
        val javadocJar = tasks.register<Jar>("${publication.name}JavadocJar") {
            archiveClassifier.set("javadoc")
            // Each archive name should be distinct. Mirror the format for the sources Jar tasks.
            archiveBaseName.set("${archiveBaseName.orNull ?: project.name}-${publication.name}")
        }
        artifact(javadocJar)
    }
}

kotlin {
    @targets@

    sourceSets.commonMain {
        dependencies {
            compileOnly(kotlin("stdlib-common"))
            @dependencies@
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-module-name", "co.touchlab.skie:runtime-kotlin")
    }
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-module-name", "co.touchlab.skie:runtime-kotlin")
    }
}
