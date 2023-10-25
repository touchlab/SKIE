package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import co.touchlab.skie.oir.element.OirTypeParameter
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

class KirTypeParameter(
    val descriptor: TypeParameterDescriptor,
    val parent: KirClass,
) : KirElement {

    lateinit var oirTypeParameter: OirTypeParameter

    val configuration: KirConfiguration = KirConfiguration(parent.configuration)

    init {
        parent.typeParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $descriptor>"
}
