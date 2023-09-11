package co.touchlab.skie.api.phases

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope

class GenerateSwiftCodeUsingSwiftPoetPhase(
    private val skieModule: DefaultSkieModule,
    private val swiftModelScope: DefaultSwiftModelScope,
) : SkieLinkingPhase {

    override fun execute() {
        with(swiftModelScope) {
            skieModule.produceSwiftPoetFiles(swiftModelScope)
            skieModule.produceTextFiles()
        }
    }
}
