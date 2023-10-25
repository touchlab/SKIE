package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration

sealed interface KirClassParent : KirElement {

    val configuration: KirConfiguration

    val module: KirModule

    val classes: MutableList<KirClass>
}
