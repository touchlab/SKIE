plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)

    implementation(projects.api)
    implementation(projects.spi)
    implementation(projects.reflector)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
