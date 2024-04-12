package co.touchlab.skie.test.util

import io.kotest.matchers.collections.shouldBeEmpty
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

fun BuildResult.shouldBeSuccess() {
    this.tasks(TaskOutcome.FAILED).shouldBeEmpty()
}
