package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.util.stringListProperty
import org.gradle.api.Project
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

private fun Project.kotlinToolingVersionsAxis(): KotlinToolingVersionAxis {
    val requestedIdentifiers = project.stringListProperty("versionSupport.kotlinTooling")
    return ToolingVersions.Kotlin.dependencyAxisFrom(requestedIdentifiers)
}

private fun Project.gradleApiVersionsAxis(): GradleApiVersionAxis {
    val requestedIdentifiers = project.stringListProperty("versionSupport.gradleApi")
    return ToolingVersions.Gradle.dependencyAxisFrom(requestedIdentifiers)
}

fun Project.kotlinToolingVersions(): SingleAxisDependencyMatrix<KotlinToolingVersion> {
    val axis = kotlinToolingVersionsAxis()
    return SingleAxisDependencyMatrix(axis)
}

fun Project.gradleApiVersions(): SingleAxisDependencyMatrix<GradleApiVersion> {
    val axis = gradleApiVersionsAxis()
    return SingleAxisDependencyMatrix(axis)
}

fun Project.kotlinPluginShimVersions(): KotlinPluginShimDependencyMatrix {
    return KotlinPluginShimDependencyMatrix(
        kotlinToolingVersionsAxis(),
        gradleApiVersionsAxis(),
    )
}
