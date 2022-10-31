plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.buildconfig) apply false

    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.skie.dev-support"
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
