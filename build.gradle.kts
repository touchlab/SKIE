plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.swiftgen"
    version = "0.1"
}

tasks.create("cleanAll") {
    dependsOn(gradle.includedBuild("core").task(":cleanAll"))

    allprojects.forEach { project ->
        project.afterEvaluate {
            project.tasks.findByName("clean")?.let {
                dependsOn(it)
            }
        }
    }
}
