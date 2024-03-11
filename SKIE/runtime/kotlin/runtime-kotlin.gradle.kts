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
    // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
    // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
    // This solution is far from ideal due to current project setup limitations - refactor this code as part of the build logic rewrite
    sourceSets.configureEach {
        val nameSegments = name.split("kgp_")
        if (nameSegments.size == 2) {
            val kgpVersionSegment = nameSegments[1]
            dependencies {
                if (kgpVersionSegment.startsWith("1.8.0")) {
                    implementation(libs.kotlinx.coroutines.core.legacy)
                } else {
                    implementation(libs.kotlinx.coroutines.core)
                }
            }
        }
    }
}
