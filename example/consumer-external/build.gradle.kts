plugins {
    kotlin("multiplatform") version "1.7.0"
    kotlin("native.cocoapods") version "1.7.0"
    id("co.touchlab.swiftpack")
}

kotlin {
    ios()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    tvos()
    tvosSimulatorArm64()
    watchos()
    watchosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("co.touchlab.swiftpack.example:example-producer:1.0")
                api("co.touchlab.swiftpack.example:example-nonproducer:1.0")
            }
        }
        val commonTest by getting

        val darwinMain by creating {
            dependsOn(commonMain)
        }
        val darwinTest by creating {
            dependsOn(commonTest)
        }

        val iosMain by getting {
            dependsOn(darwinMain)
        }
        val iosTest by getting {
            dependsOn(darwinTest)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }

        val macosMain by creating {
            dependsOn(darwinMain)
        }
        val macosTest by creating {
            dependsOn(darwinTest)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val macosArm64Test by getting {
            dependsOn(macosTest)
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val macosX64Test by getting {
            dependsOn(macosTest)
        }

        val tvosMain by getting {
            dependsOn(darwinMain)
        }
        val tvosTest by getting {
            dependsOn(darwinTest)
        }
        val tvosSimulatorArm64Main by getting {
            dependsOn(tvosMain)
        }
        val tvosSimulatorArm64Test by getting {
            dependsOn(tvosTest)
        }

        val watchosMain by getting {
            dependsOn(darwinMain)
        }
        val watchosTest by getting {
            dependsOn(darwinTest)
        }
        val watchosSimulatorArm64Main by getting {
            dependsOn(watchosMain)
        }
        val watchosSimulatorArm64Test by getting {
            dependsOn(watchosTest)
        }
    }

    cocoapods {
        name = "ExampleKit"
        summary = "Example library for swikt"
        homepage = "https://github.com/touchlab/swikt"
        framework {
            this.isStatic = false
            baseName = "ExampleKit"
            export("co.touchlab.swiftpack.example:example-producer")
            export("co.touchlab.swiftpack.example:example-nonproducer")
        }
        ios.deploymentTarget = "12.4"
        tvos.deploymentTarget = "12.4"
        watchos.deploymentTarget = "8.0"
        osx.deploymentTarget = "11.0"
        // podfile = project.file("../app/Podfile")
    }
}
