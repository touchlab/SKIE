package co.touchlab.skie.phases.kir

import co.touchlab.skie.phases.CompilerIndependentDescriptorConversionPhase
import co.touchlab.skie.phases.DescriptorConversionPhase

object InitializeKirMembersCachePhase : CompilerIndependentDescriptorConversionPhase {

    context(DescriptorConversionPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeCallableDeclarationsCache()
    }
}
