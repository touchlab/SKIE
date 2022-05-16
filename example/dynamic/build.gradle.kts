plugins {
    id("co.touchlab.swikt")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

swikt {
}

kotlin {
    ios()
    // Note: iosSimulatorArm64 target requires that all dependencies have M1 support
    iosSimulatorArm64()

    sourceSets {
        val iosMain by getting
        val iosTest by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
    }

    cocoapods {
        summary = "Example library for swikt"
        homepage = "https://github.com/touchlab/swikt"
        framework {
            isStatic = false
            baseName = "ExampleKit"
        }
        ios.deploymentTarget = "12.4"
        podfile = project.file("../app/Podfile")
    }
}

