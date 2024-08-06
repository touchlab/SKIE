import co.touchlab.skie.buildsetup.plugins.MultiCompileTarget

plugins {
    id("skie.base")
    id("skie.multicompile")
}

multiCompileRuntime {
    isPublishable = true
    targets.set(MultiCompileTarget.allDarwin)
    klibPath = { kotlinVersion, target ->
        "build/classes/kotlin/${target.name}/main/klib/runtime-kotlin-${kotlinVersion}.klib"
    }
    dependencies = { kotlinVersion ->
        if (kotlinVersion == MultiCompileTarget.kotlin_1_8_0) {
            "implementation(libs.kotlinx.coroutines.core.legacy)"
        } else {
            "implementation(libs.kotlinx.coroutines.core)"
        }
    }
    applyDependencies = { kotlinVersion, configuration ->
        configuration(
            if (kotlinVersion == MultiCompileTarget.kotlin_1_8_0) {
                libs.kotlinx.coroutines.core.legacy
            } else {
                libs.kotlinx.coroutines.core
            }
        )
    }
}
