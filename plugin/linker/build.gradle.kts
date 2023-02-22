plugins {
    id("skie-jvm")
    id("skie-publish-jvm")

    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)

    implementation(projects.api)
    implementation(projects.spi)
    implementation(projects.reflector)

    implementation(libs.kotlinx.serialization.yaml)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
