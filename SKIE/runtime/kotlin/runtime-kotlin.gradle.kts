plugins {
    id("skie.runtime.kotlin")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Runtime - Kotlin"
    description = "Kotlin Multiplatform part of the SKIE runtime. It's used to facilitate certain features of SKIE."
    publishSources = true
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
