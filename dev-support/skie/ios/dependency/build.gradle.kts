plugins {
    id("dev.multiplatform")
}

kotlin {
    ios()
    iosSimulatorArm64()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("co.touchlab.skie:configuration-annotations")
        }
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
