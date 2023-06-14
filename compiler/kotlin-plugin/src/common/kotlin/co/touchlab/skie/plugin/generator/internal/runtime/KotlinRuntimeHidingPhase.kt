package co.touchlab.skie.plugin.generator.internal.runtime

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

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
                    it.swiftModel.visibility = SwiftModelVisibility.Hidden
                }
        }
    }
}
