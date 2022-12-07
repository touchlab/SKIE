plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false

    id("gradle-src-classpath-loader")
    id("co.touchlab.touchlabtools.docusaurusosstemplate") version "0.1.8"
}

allprojects {
    group = "co.touchlab.skie"
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("cleanAll") {
    dependsOn(gradle.includedBuilds.map { it.task(":cleanAll") })
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
