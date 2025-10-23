package co.touchlab.skie.buildsetup.util

import org.gradle.api.Project

// WIP

val Project.mavenArtifactId: String
    get() = "skie" + this.path.replace(":", "-")

val Project.dependencyModule: String
    get() = "$group:$name"

val Project.dependencyCoordinate: String
    get() = "$dependencyModule:$version"
