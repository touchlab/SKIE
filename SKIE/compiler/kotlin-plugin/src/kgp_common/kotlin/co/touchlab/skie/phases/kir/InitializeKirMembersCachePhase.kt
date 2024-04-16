package co.touchlab.skie.phases.kir

import co.touchlab.skie.phases.DescriptorConversionPhase

object InitializeKirMembersCachePhase : DescriptorConversionPhase {

    context(DescriptorConversionPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeCallableDeclarationsCache()
    }
}
