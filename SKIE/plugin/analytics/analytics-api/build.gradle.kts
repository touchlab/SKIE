plugins {
    id("skie-jvm")
    id("skie-publish-jvm")

    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.pluginConfigurationApi)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
