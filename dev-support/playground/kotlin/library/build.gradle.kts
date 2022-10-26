plugins {
    kotlin("multiplatform")
}

kotlin {
    macosX64()
    macosArm64()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.skie:configuration-annotations")
        }
    }

    val macosMain by sourceSets.creating {
        dependsOn(commonMain)
    }

    val macosArm64Main by sourceSets.getting {
        dependsOn(macosMain)
    }

    val macosX64Main by sourceSets.getting {
        dependsOn(macosMain)
    }
}
