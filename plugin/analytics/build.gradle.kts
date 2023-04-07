plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.api)
    implementation(projects.api.air)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
