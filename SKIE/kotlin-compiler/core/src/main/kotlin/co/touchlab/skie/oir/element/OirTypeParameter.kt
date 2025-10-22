package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.TypeParameterUsageOirType
import co.touchlab.skie.sir.element.SirTypeParameter

class OirTypeParameter(
    val name: String,
    val parent: OirClass,
    val variance: Variance,
    bounds: List<OirType> = emptyList(),
) : OirElement {

    var sirTypeParameter: SirTypeParameter? = null

    val bounds: MutableList<OirType> = bounds.toMutableList()

    init {
        parent.typeParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name : ${bounds.joinToString("&")}"

    enum class Variance {
        Invariant,
        Covariant,
        Contravariant
    }
}

fun OirTypeParameter.toTypeParameterUsage(): TypeParameterUsageOirType =
    TypeParameterUsageOirType(this)
