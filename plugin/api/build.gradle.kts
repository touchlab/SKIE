plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.swiftPoet)
    api(projects.util)
    api(projects.configurationApi)
    api(projects.analytics.producer)
    implementation(projects.reflector)

    compileOnly(libs.kotlin.native.compiler.embeddable)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
