plugins {
    id("skie.common")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Configuration API"
    description = "Module providing basic configuration API for SKIE."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
    }
}
