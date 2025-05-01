package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.type.TypeParameterUsageKirType
import co.touchlab.skie.oir.element.OirTypeParameter

class KirTypeParameter(val name: String, val parent: KirClass, val variance: OirTypeParameter.Variance) : KirElement {

    lateinit var oirTypeParameter: OirTypeParameter

    init {
        parent.typeParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"
}

fun KirTypeParameter.toTypeParameterUsage(): TypeParameterUsageKirType = TypeParameterUsageKirType(this)
