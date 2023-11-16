package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.sir.element.SirProperty
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class KirEnumEntry(
    val descriptor: ClassDescriptor,
    val owner: KirClass,
    val index: Int,
) : KirElement {

    lateinit var oirEnumEntry: OirProperty

    val sirEnumEntry: SirProperty
        get() = oirEnumEntry.originalSirProperty

    val configuration: KirConfiguration = KirConfiguration(owner.configuration)

    init {
        owner.enumEntries.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $descriptor"
}
