plugins {
    id("skie-multiplatform")
}

kotlin {
    macosX64()
    macosArm64()
    ios()
    iosSimulatorArm64()

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

    val iosMain by sourceSets.getting
    val iosTest by sourceSets.getting

    val iosSimulatorArm64Main by sourceSets.getting {
        dependsOn(iosMain)
    }
    val iosSimulatorArm64Test by sourceSets.getting {
        dependsOn(iosTest)
    }
}
