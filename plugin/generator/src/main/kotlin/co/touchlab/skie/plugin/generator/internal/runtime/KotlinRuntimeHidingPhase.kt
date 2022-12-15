package co.touchlab.skie.plugin.generator.internal.runtime

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class KotlinRuntimeHidingPhase(
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun execute(descriptorProvider: DescriptorProvider) {
        skieContext.module.configure {
            descriptorProvider.classDescriptors
                .filter { it.belongsToSkieRuntime }
                .forEach {
                    it.isHiddenFromSwift = true
                }
        }
    }
}
