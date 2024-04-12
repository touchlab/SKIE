package co.touchlab.skie.test.util

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

fun BuildTask?.shouldBeSuccess() {
    this.shouldNotBeNull()
        .outcome shouldBe TaskOutcome.SUCCESS
}
