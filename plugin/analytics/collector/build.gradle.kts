plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.analytics.analyticsApi)
    implementation(projects.api)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
