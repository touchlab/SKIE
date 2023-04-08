plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
