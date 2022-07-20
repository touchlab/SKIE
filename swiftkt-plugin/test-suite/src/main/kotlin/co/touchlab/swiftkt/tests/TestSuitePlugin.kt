package co.touchlab.swiftkt.tests

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class TestSuitePlugin: Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        tasks.register("swiktTestScenarios", TestScenariosTask::class.java).configure {
            outputs.upToDateWhen { false }
        }

        tasks.register("integrationTests", IntegrationTestTask::class.java).configure {
            configure()
        }
    }
}
