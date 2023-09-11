package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.element.SirTypeParameter
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

data class TypeParameterUsageSirType(
    val typeParameter: SirTypeParameter,
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = typeParameter.bounds.any { it.isHashable }

    override val isPrimitive: Boolean
        get() = typeParameter.bounds.any { it.isPrimitive }

    override val directlyReferencedTypes: List<SirType>
        get() = typeParameter.bounds

    override fun toSwiftPoetUsage(): TypeName =
        TypeVariableName(typeParameter.name)
}
