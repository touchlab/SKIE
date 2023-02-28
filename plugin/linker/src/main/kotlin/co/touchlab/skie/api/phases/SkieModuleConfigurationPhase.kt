package co.touchlab.skie.api.phases

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope

class SkieModuleConfigurationPhase(
    private val skieModule: DefaultSkieModule,
    private val swiftModelScope: MutableSwiftModelScope,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.consumeConfigureBlocks(swiftModelScope)
    }
}
