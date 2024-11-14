import co.touchlab.skie.buildsetup.plugins.MultiCompileTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("skie.base")
    id("skie.multicompile")
}

multiCompileRuntime {
    isPublishable = true
    targets.set(MultiCompileTarget.allDarwin)
    klibPath = { kotlinVersion, target ->
        val includeKlibExtension = true // kotlinVersion < MultiCompileTarget.kotlin_2_1_0
        "build/classes/kotlin/${target.name}/main/klib/runtime-kotlin-${kotlinVersion}" + if (includeKlibExtension) ".klib" else ""
    }
    dependencies = { _ ->
        "implementation(libs.kotlinx.coroutines.core)"
    }
    applyDependencies = { _, configuration ->
        configuration(
            libs.kotlinx.coroutines.core
        )
    }
}
