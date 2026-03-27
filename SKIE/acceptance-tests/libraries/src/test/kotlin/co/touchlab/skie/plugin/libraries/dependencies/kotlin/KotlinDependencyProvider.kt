package co.touchlab.skie.plugin.libraries.dependencies.kotlin

import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.library.Artifact
import co.touchlab.skie.plugin.libraries.library.Artifacts
import co.touchlab.skie.plugin.libraries.library.Component
import org.gradle.tooling.BuildException
import org.gradle.tooling.GradleConnector
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.AutoCloseable
import kotlin.io.path.Path

class KotlinDependencyProvider : AutoCloseable {

    private val useDependencyConstraints = "ignoreDependencyConstraints" !in TestProperties

    private val gradleConnection = GradleConnector.newConnector()
        .forProjectDirectory(File(TestBuildConfig.LIBRARY_TESTS_DEPENDENCY_RESOLVER_PATH))
        .connect()

    override fun close() {
        gradleConnection.close()
    }

    fun resolveArtifacts(primaryLibrary: Component, constraints: List<Component>): Result<Artifacts> =
        resolveAllArtifacts(primaryLibrary, constraints)
            .map { artifacts ->
                Artifacts(
                    all = artifacts,
                    exported = resolveExportedArtifacts(artifacts, primaryLibrary),
                )
            }

    private fun resolveExportedArtifacts(artifacts: List<Artifact>, primaryLibrary: Component): List<Artifact> =
        artifacts.filter { it.component == primaryLibrary }

    private fun resolveAllArtifacts(primaryLibrary: Component, constraints: List<Component>): Result<List<Artifact>> {
        val outputStream = ByteArrayOutputStream()

        try {
            callGradle(primaryLibrary, constraints, outputStream)
        } catch (e: RuntimeException) {
            return Result.failure(BuildException(outputStream.toString(), e))
        }

        val output = outputStream.toString()

        val artifacts = parseArtifacts(output)

        return Result.success(artifacts)
    }

    private fun callGradle(primaryLibrary: Component, constraints: List<Component>, outputStream: ByteArrayOutputStream) {
        gradleConnection.newBuild()
            .forTasks(":noop")
            .addArguments("-Plibrary=$primaryLibrary")
            .apply {
                if (useDependencyConstraints && constraints.isNotEmpty()) {
                    addArguments("-Pconstraints=${constraints.joinToString("|")}")
                }
            }
            .setStandardOutput(outputStream)
            .setStandardError(outputStream)
            .run()
    }

    private fun parseArtifacts(output: String): List<Artifact> =
        output
            .substringAfter("<libraries-start>")
            .substringBefore("<libraries-end>")
            .trim()
            .lines()
            .filter { it.isNotBlank() }
            .map {
                val (stringCoordinate, path) = it.trim().split("|")

                val (group, name, version) = stringCoordinate.split(":")

                val component = Component(group, name, version)

                Artifact(component, Path(path))
            }
}
