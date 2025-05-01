package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirTypeParameterParent
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType

class SirTypeParameter(
    val name: String,
    parent: SirTypeParameterParent,
    bounds: List<Bound> = emptyList(),
    val isPrimaryAssociatedType: Boolean = false,
) : SirElement {

    val bounds: MutableList<Bound> = bounds.toMutableList()

    var parent: SirTypeParameterParent by sirTypeParameterParent(parent)

    override fun toString(): String = "${this::class.simpleName}: $name : ${bounds.joinToString("&")}>"

    sealed interface Bound {
        val type: SirType

        class Equality(override val type: SirType) : Bound {
            override fun copy(type: SirType) = Equality(type)
        }
        class Conformance(override val type: SirType) : Bound {
            override fun copy(type: SirType) = Conformance(type)
        }

        fun copy(type: SirType): Bound
    }

    companion object {

        context(SirTypeParameterParent)
        operator fun invoke(name: String, bounds: List<Bound> = emptyList(), isPrimaryAssociatedType: Boolean = false): SirTypeParameter =
            SirTypeParameter(
                name = name,
                parent = this@SirTypeParameterParent,
                bounds = bounds,
                isPrimaryAssociatedType = isPrimaryAssociatedType,
            )

        context(SirTypeParameterParent)
        operator fun invoke(name: String, vararg bounds: Bound, isPrimaryAssociatedType: Boolean = false): SirTypeParameter =
            SirTypeParameter(
                name = name,
                bounds = bounds.toList(),
                isPrimaryAssociatedType = isPrimaryAssociatedType,
            )
    }
}

fun SirTypeParameter.toTypeParameterUsage(): TypeParameterUsageSirType = TypeParameterUsageSirType(this)

fun SirType.toEqualityBound(): SirTypeParameter.Bound.Equality = SirTypeParameter.Bound.Equality(this)

fun SirType.toConformanceBound(): SirTypeParameter.Bound.Conformance = SirTypeParameter.Bound.Conformance(this)
