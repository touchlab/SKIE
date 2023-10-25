package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirTypeParameterParent
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType

class SirTypeParameter(
    val name: String,
    parent: SirTypeParameterParent,
    bounds: List<SirType> = emptyList(),
) : SirElement {

    val bounds: MutableList<SirType> = bounds.toMutableList()

    var parent: SirTypeParameterParent by sirTypeParameterParent(parent)

    override fun toString(): String = "${this::class.simpleName}: $name : ${bounds.joinToString("&")}>"

    companion object {

        context(SirTypeParameterParent)
        operator fun invoke(
            name: String,
            bounds: List<SirType> = emptyList(),
        ): SirTypeParameter =
            SirTypeParameter(
                name = name,
                parent = this@SirTypeParameterParent,
                bounds = bounds,
            )

        context(SirTypeParameterParent)
        operator fun invoke(
            name: String,
            vararg bounds: SirType,
        ): SirTypeParameter =
            SirTypeParameter(
                name = name,
                bounds = bounds.toList(),
            )
    }
}

fun SirTypeParameter.toTypeParameterUsage(): TypeParameterUsageSirType =
    TypeParameterUsageSirType(this)
