plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.swiftPoet)
    api(projects.configurationApi)
    implementation(projects.reflector)

    compileOnly(libs.kotlin.native.compiler.embeddable)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
