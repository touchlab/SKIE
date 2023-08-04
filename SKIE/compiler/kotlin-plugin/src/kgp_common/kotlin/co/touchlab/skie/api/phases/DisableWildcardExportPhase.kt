package co.touchlab.skie.api.phases

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.util.FrameworkLayout

class DisableWildcardExportPhase(
    private val skieContext: SkieContext,
    private val framework: FrameworkLayout,
) : SkieLinkingPhase {

    override val isActive: Boolean
        get() = SkieConfigurationFlag.Migration_WildcardExport !in skieContext.skieConfiguration.enabledConfigurationFlags

    override fun execute() {
        framework.modulemapFile.writeText(
            framework.modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator())
        )
    }
}
