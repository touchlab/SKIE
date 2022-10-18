plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
}
