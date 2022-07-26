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
            }
        }
    }
}