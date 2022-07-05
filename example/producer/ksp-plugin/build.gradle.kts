plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.0-1.0.6")
    implementation("co.touchlab.swiftpack:swiftpack-api")

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}