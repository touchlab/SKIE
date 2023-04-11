plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    id("skie-buildconfig")
}

buildConfig {
    val kotlinPlugin = projects.kotlinPlugin.dependencyProject

    buildConfigField("String", "SKIE_VERSION", "\"${kotlinPlugin.version}\"")
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)
    implementation(libs.logback)

    implementation(projects.kotlinPlugin.options)
    implementation(projects.api)
    implementation(projects.configurationApi)
    implementation(projects.interceptor)
    implementation(projects.generator)
    implementation(projects.linker)
    implementation(projects.license)
    implementation(projects.reflector)
    implementation(projects.spi)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
