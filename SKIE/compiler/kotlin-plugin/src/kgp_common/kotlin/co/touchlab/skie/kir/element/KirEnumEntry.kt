package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class KirEnumEntry(
    val descriptor: ClassDescriptor,
    val swiftName: String,
    val parent: KirClass,
) : KirElement {

    val configuration: KirConfiguration = KirConfiguration(parent.configuration)

    init {
        parent.enumEntries.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $descriptor"
}
