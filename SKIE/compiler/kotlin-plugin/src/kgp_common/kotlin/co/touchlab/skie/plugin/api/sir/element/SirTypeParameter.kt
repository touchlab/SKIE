package co.touchlab.skie.plugin.api.sir.element

import co.touchlab.skie.plugin.api.sir.element.util.sirTypeParameterParent
import co.touchlab.skie.plugin.api.sir.type.SirType
import co.touchlab.skie.plugin.api.sir.type.TypeParameterUsageSirType
import io.outfoxx.swiftpoet.TypeVariableName

class SirTypeParameter(
    val name: String,
    parent: SirTypeParameterParent,
    bounds: List<SirType> = emptyList(),
) : SirElement {

    val bounds: MutableList<SirType> = bounds.toMutableList()

    var parent: SirTypeParameterParent by sirTypeParameterParent(parent)

    fun toSwiftPoetVariable() = TypeVariableName.typeVariable(
        name,
        bounds.map { TypeVariableName.Bound(it.toSwiftPoetUsage()) },
    )

    override fun toString(): String = "type parameter: $name : ${bounds.joinToString("&")}>"
}

fun SirTypeParameter.toTypeParameterUsage(): TypeParameterUsageSirType =
    TypeParameterUsageSirType(this)

fun List<SirTypeParameter>.toSwiftPoetVariables(): List<TypeVariableName> =
    map { it.toSwiftPoetVariable() }
