import co.touchlab.skie.buildsetup.plugins.MultiCompileTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("skie.runtime")
    id("skie.multicompile")
}

multiCompileRuntime {
    isPublishable = true
    targets.set(MultiCompileTarget.all)
    rootKotlinVersion = MultiCompileTarget.kotlin_2_0_0
    klibPath = { kotlinVersion, target ->
        if (target.platformType == KotlinPlatformType.jvm) {
            "build/libs/configuration-annotations-${kotlinVersion}-${target.name.lowercase()}-${version}.jar"
        } else if (kotlinVersion >= MultiCompileTarget.kotlin_2_1_0) {
            "build/libs/configuration-annotations-${kotlinVersion}-${target.name}Main-${version}.klib"
        } else {
            "build/classes/kotlin/${target.name}/main/klib/configuration-annotations-${kotlinVersion}.klib"
        }
    }
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxArm64()
    linuxX64()

    mingwX64()

    sourceSets.commonMain {
        kotlin.srcDirs("impl/src/commonMain/kotlin")
    }
}
