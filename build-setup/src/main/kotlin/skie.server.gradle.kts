
import co.touchlab.skie.gradle.util.libs
import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm")
}

group = "co.touchlab.skie.server"

kotlin {
    jvmToolchain(libs.versions.java)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.bundles.testing.jvm)
}
