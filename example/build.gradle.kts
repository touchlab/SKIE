import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("co.touchlab.swikt") apply false
    kotlin("multiplatform") apply false
    kotlin("native.cocoapods") apply false
    idea

    id("co.touchlab.swikt.test-suite")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    the<KotlinMultiplatformExtension>().apply {
        ios()
        iosSimulatorArm64()
        macosArm64()
        macosX64()
        tvos()
        tvosSimulatorArm64()
        watchos()
        watchosSimulatorArm64()

        sourceSets {
            val commonMain by getting
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
    }
}

subprojects {
    apply(plugin = "co.touchlab.swikt")
    apply(plugin = "org.jetbrains.kotlin.native.cocoapods")

    val isStatic = this.name.endsWith("static")

    (the<KotlinMultiplatformExtension>() as ExtensionAware).the<CocoapodsExtension>().apply {
        name = "ExampleKit_${this@subprojects.name.capitalized()}"
        summary = "Example library for swikt"
        homepage = "https://github.com/touchlab/swikt"
        framework {
            this.isStatic = isStatic
            baseName = "ExampleKit"
        }
        ios.deploymentTarget = "12.4"
        tvos.deploymentTarget = "12.4"
        watchos.deploymentTarget = "8.0"
        osx.deploymentTarget = "11.0"
        podfile = project.file("../app/Podfile")
    }
}

afterEvaluate {
    val sourceSetNames = the<KotlinMultiplatformExtension>().sourceSets.map { it.name }.toSet()
    idea.module.sourceDirs.addAll(
        sourceSetNames.flatMap {
            listOf(
                file("common/src/$it/kotlin"),
                file("common/src/$it/swift"),
            )
        }
    )
}
