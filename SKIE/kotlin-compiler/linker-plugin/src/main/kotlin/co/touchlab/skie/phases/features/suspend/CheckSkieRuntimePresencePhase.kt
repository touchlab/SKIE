package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.descriptorProvider
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

object CheckSkieRuntimePresencePhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        val skieRuntimeClassFqName = SupportedFlow.allVariants.first().kotlinClassFqName

        val isRuntimePresent = descriptorProvider.exposedClasses.any { it.fqNameUnsafe.toString() == skieRuntimeClassFqName }

        if (!isRuntimePresent) {
            SkieConfigurationFlag.Feature_CoroutinesInterop.disable()
        }
    }
}
