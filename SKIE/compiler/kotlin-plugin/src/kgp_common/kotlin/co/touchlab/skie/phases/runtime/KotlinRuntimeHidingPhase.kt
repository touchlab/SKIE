package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkieCompilationPhase

internal class KotlinRuntimeHidingPhase(
    private val descriptorProvider: DescriptorProvider,
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        skieContext.module.configure {
            descriptorProvider.exposedClasses
                .filter { it.belongsToSkieRuntime }
                .forEach {
                    it.swiftModel.visibility = co.touchlab.skie.swiftmodel.SwiftModelVisibility.Hidden
                }
        }
    }
}
