package co.touchlab.skie.kir.element

import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.sir.element.SirProperty

class KirEnumEntry(
    val kotlinName: String,
    val objCSelector: String,
    val swiftName: String,
    val owner: KirClass,
    val index: Int,
    val hasUserDefinedName: Boolean,
) : KirElement {

    lateinit var oirEnumEntry: OirProperty

    val sirEnumEntry: SirProperty
        get() = oirEnumEntry.originalSirProperty

    init {
        owner.enumEntries.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $kotlinName"
}
