import co.touchlab.skie.gradle.util.libs

plugins {
    kotlin("jvm")
}

group = "co.touchlab.skie"

kotlin {
    jvmToolchain(libs.versions.java)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(libs.bundles.testing.jvm)
}
