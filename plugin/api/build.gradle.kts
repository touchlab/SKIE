plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(projects.util)
    api(projects.license)
    api(projects.configurationApi)
    api(projects.analytics.producer)
    api(projects.analytics.analyticsApi)
    implementation(projects.reflector)
    implementation(libs.kotlinx.serialization.json)

    compileOnly(libs.kotlin.native.compiler.embeddable)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
