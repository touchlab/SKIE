package co.touchlab.skie.phases

import co.touchlab.skie.swiftmodel.MutableSwiftModelScope

class SkieModuleConfigurationPhase(
    private val skieModule: DefaultSkieModule,
    private val swiftModelScope: MutableSwiftModelScope,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.consumeConfigureBlocks(swiftModelScope)
    }
}
