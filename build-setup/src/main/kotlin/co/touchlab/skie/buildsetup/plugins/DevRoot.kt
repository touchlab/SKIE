package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.named

abstract class DevRoot : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        tasks.named<Wrapper>("wrapper") {
            distributionType = Wrapper.DistributionType.ALL
        }

        tasks.register("cleanAll") {
            dependsOn(gradle.includedBuilds.map { it.task(":cleanAll") })
            dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
        }
    }
}
