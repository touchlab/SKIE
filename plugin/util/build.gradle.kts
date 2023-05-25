plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    id("skie-buildconfig")
    alias(libs.plugins.kotlin.plugin.serialization)
}

buildConfig {
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
