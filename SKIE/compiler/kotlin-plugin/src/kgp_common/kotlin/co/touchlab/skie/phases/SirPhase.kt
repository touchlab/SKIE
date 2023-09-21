package co.touchlab.skie.phases

import co.touchlab.skie.phases.header.util.HeaderDeclarationsProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope

interface SirPhase : SkiePhase<SirPhase.Context> {

    interface Context : SkiePhase.Context, MutableSwiftModelScope {

        override val context: Context

        val headerDeclarationsProvider: HeaderDeclarationsProvider

        val swiftModelProvider: MutableSwiftModelScope
            get() = this
    }
}
