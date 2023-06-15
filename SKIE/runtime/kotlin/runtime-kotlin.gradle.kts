import co.touchlab.skie.gradle.kotlin.apple

plugins {
    id("skie-multiplatform")
    id("skie-publish-multiplatform")
}

kotlin {
    apple()

    sourceSets.commonMain {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
