package co.touchlab.skie.kir

import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule

interface KirProviderDelegate {

    val kotlinModules: Collection<KirModule>

    val stdlibModule: KirModule

    val kirBuiltins: KirBuiltins

    val allExternalClassesAndProtocols: Collection<KirClass>
}
