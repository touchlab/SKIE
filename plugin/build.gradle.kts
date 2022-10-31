plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.versionCheck)

    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.skie"
    version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }
}

// The name is a workaround for (probably) a Gradle bug - something adds a conflicting "clean" task
val cleanRoot by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}

//tasks.withType<DependencyUpdatesTask> {
//    rejectVersionIf {
//        candidate.version.isNonStable()
//    }
//}
//
//fun String.isNonStable() = "^[0-9,.v-]+(-r)?$".toRegex().matches(this).not()

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("cleanAll") {
    dependsOn(cleanRoot)
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
