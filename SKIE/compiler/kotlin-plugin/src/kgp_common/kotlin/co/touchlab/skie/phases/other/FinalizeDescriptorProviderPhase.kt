package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.DescriptorModificationPhase

object FinalizeDescriptorProviderPhase : DescriptorModificationPhase {

    context(DescriptorModificationPhase.Context)
    override suspend fun execute() {
        descriptorProvider.finalize()
    }
}
