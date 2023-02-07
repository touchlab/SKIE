package co.touchlab.skie.plugin.libraries

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

data class ExternalLibraryTestResult(
    val gradle: BuildResult,
    val outcomes: List<Pair<ExternalLibraryTest, TaskOutcome>>,
)
