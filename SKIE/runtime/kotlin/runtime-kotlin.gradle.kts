import co.touchlab.skie.buildsetup.plugins.MultiCompileTarget

plugins {
    id("skie.base")
    id("skie.multicompile")
}

multiCompileRuntime {
    isPublishable = true
    targets.set(MultiCompileTarget.allDarwin)
    klibPath = { kotlinVersion, target ->
        if (kotlinVersion >= MultiCompileTarget.kotlin_2_1_0) {
            "build/libs/runtime-kotlin-$kotlinVersion-${target.name}Main-$version.klib"
        } else {
            "build/classes/kotlin/${target.name}/main/klib/runtime-kotlin-$kotlinVersion.klib"
        }
    }
    dependencies = { _ ->
        "implementation(libs.kotlinx.coroutines.core)"
    }
    applyDependencies = { _, configuration ->
        configuration(
            libs.kotlinx.coroutines.core,
        )
    }
}
