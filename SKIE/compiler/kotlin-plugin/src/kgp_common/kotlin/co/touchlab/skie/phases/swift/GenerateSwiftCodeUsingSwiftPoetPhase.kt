package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.DefaultSkieModule
import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.swiftmodel.DefaultSwiftModelScope

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
