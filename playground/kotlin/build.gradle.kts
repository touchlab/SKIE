import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.swiftpack)
    alias(libs.plugins.swiftkt)
    id("co.touchlab.swiftgen.sealed")
}

kotlin {
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
            }
        }
    }

    val commonMain by sourceSets.getting {
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