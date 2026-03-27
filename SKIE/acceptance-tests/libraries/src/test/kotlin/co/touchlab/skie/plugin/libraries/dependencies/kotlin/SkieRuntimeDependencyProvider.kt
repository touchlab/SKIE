package co.touchlab.skie.plugin.libraries.dependencies.kotlin

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.library.Artifact
import co.touchlab.skie.plugin.libraries.library.Artifacts
import co.touchlab.skie.plugin.libraries.library.Component
import kotlin.io.path.Path

object SkieRuntimeDependencyProvider {

    private val skieKotlinRuntime = Artifact(
        Component("co.touchlab.skie", "runtime-kotlin", "?"),
        Path(TestBuildConfig.SKIE_IOS_ARM64_KOTLIN_RUNTIME_KLIB_PATH),
    )

    fun withSkieRuntimeIfRequired(artifacts: Artifacts, skieConfigurationData: CompilerSkieConfigurationData?): Artifacts =
        if (isSkieRuntimeIfRequired(artifacts, skieConfigurationData)) {
            Artifacts(
                all = artifacts.all + skieKotlinRuntime,
                exported = artifacts.exported + skieKotlinRuntime,
            )
        } else {
            artifacts
        }

    private fun isSkieRuntimeIfRequired(artifacts: Artifacts, skieConfigurationData: CompilerSkieConfigurationData?): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in (skieConfigurationData?.enabledConfigurationFlags ?: emptySet()) &&
            artifacts.all.containsCoroutines()

    private fun List<Artifact>.containsCoroutines(): Boolean =
        any { it.component.coordinate.startsWith("org.jetbrains.kotlinx:kotlinx-coroutines-core") }
}
