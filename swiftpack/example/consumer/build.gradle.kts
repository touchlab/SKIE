plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("co.touchlab.swiftpack")
    `maven-publish`
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
                api(projects.exampleProducer)
                api(projects.exampleNonproducer)
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
            export(projects.exampleProducer)
            export(projects.exampleNonproducer)
        }
        ios.deploymentTarget = "12.4"
        tvos.deploymentTarget = "12.4"
        watchos.deploymentTarget = "8.0"
        osx.deploymentTarget = "11.0"
        // podfile = project.file("../app/Podfile")
    }
}

swiftPack {
    isPublishingEnabled.set(false)
}