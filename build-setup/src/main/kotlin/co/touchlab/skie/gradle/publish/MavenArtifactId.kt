package co.touchlab.skie.gradle.publish

import org.gradle.api.Project

val Project.mavenArtifactId: String
    get() = "skie" + this.path.replace(":", "-")
