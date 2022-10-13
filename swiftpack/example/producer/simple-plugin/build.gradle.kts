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
    implementation("co.touchlab.swiftpack:swiftpack-api")
    implementation("co.touchlab.swiftpack:swiftpack-plugin-api")

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}