plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.skie"
    version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"
}

// Workaround for KMM plugin bug - JS target adds clean task for root project
val cleanRoot by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("cleanAll") {
    dependsOn(cleanRoot)
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
