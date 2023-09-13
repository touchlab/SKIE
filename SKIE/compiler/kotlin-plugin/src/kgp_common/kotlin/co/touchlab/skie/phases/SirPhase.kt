package co.touchlab.skie.phases

import co.touchlab.skie.swiftmodel.MutableSwiftModelScope

interface SirPhase : SkiePhase<SirPhase.Context> {

    interface Context : SkiePhase.Context, MutableSwiftModelScope {

        override val context: Context
            get() = this

        val swiftModelProvider: MutableSwiftModelScope
            get() = this
    }
}
