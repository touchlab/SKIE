import co.touchlab.skie.gradle.util.libs

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(libs.versions.java)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.bundles.testing.jvm)
}
