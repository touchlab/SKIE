import co.touchlab.skie.gradle.publish.publishCode

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

publishCode()

dependencies {
    implementation(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
}
