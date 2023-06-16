package co.touchlab.skie.gradle.publish

import org.gradle.api.Project

val Project.mavenArtifactId: String
    get() = "skie" + this.path.replace(":", "-")

val Project.dependencyName: String
    get() = "$group:$name:$version"
