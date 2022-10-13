plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
    implementation("co.touchlab.swiftpack:swiftpack-api")
    implementation("co.touchlab.swiftpack:swiftpack-plugin-api")

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}