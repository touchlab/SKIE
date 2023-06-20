import co.touchlab.skie.gradle.kotlin.apple

plugins {
    id("skie.runtime.kotlin")
    id("skie.publishable")
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
