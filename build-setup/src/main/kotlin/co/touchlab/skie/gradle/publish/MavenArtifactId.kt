package co.touchlab.skie.gradle.publish

import org.gradle.api.Project

val Project.mavenArtifactId: String
    get() = "skie" + this.path.replace(":", "-")

val Project.dependencyModule: String
    get() = "$group:$name"

val Project.dependencyCoordinate: String
    get() = "$dependencyModule:$version"
