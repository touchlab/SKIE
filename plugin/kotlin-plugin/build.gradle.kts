plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)

    implementation(projects.kotlinPlugin.options)
    implementation(projects.api)
    implementation(projects.configurationApi)
    implementation(projects.interceptor)
    implementation(projects.generator)
    implementation(projects.linker)
    implementation(projects.reflector)
    implementation(projects.spi)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
