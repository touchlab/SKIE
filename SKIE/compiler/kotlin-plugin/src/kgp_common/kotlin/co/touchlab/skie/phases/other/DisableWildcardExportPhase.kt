package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase

object DisableWildcardExportPhase : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Migration_WildcardExport !in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        framework.modulemapFile.writeText(
            framework.modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator()),
        )
    }
}
