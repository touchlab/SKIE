plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
}
