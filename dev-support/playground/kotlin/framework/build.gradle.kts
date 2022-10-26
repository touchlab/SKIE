import co.touchlab.skie.plugin.ConfigurationKeys
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")

    id("co.touchlab.skie")
}

skie {
    configuration {
        group {
        }
    }
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
        dependencies {
            implementation(projects.playground.kotlin.library)

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
