plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)

    implementation(projects.reflector)
    implementation(projects.spi)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
