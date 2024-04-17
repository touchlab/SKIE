package co.touchlab.skie.phases

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins

interface DescriptorConversionPhase<in C : DescriptorConversionPhase.Context> : ForegroundPhase<C> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val kirProvider: KirProvider

        val kirBuiltins: KirBuiltins
    }
}
