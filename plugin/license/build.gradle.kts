plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(projects.configurationApi)
    implementation(projects.util)

    implementation(libs.java.jwt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jackson.databind)
    implementation(libs.ktor.client.java)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
