package co.touchlab.skie.phases

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins

interface KirPhase : ForegroundPhase<KirPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val kirProvider: KirProvider

        val kirBuiltins: KirBuiltins
    }
}
