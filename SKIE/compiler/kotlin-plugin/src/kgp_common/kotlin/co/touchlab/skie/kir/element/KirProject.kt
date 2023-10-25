package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration

class KirProject : KirElement {

    val configuration: KirConfiguration = KirConfiguration(null)

    val modules: MutableList<KirModule> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}"
}
