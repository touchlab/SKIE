
plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.buildconfig)
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(libs.auto.service)
    kapt(libs.auto.service)

    api(projects.swiftpackApi)
    implementation(projects.swiftpackPluginApi)
}
