plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(kotlin("stdlib"))
    implementation(libs.swiftpack.api)
    implementation(libs.swiftpack.plugin.api)

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}
