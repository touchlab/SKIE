plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false

    id("gradle-src-classpath-loader")
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
    val ignoredIncludedBuilds = listOf("swiftpoet")

    dependsOn(gradle.includedBuilds.filter { it.name !in ignoredIncludedBuilds }.map { it.task(":cleanAll") })
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
