package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

data class TypeParameterUsageSirType(
    val typeParameter: SirTypeParameter,
    val parentScope: TypeParameterUsageSirType? = null,
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = typeParameter.bounds.any { it.isHashable }

    override val isPrimitive: Boolean
        get() = typeParameter.bounds.any { it.isPrimitive }

    override val directlyReferencedTypes: List<SirType>
        get() = typeParameter.bounds

    override val canonicalName: String = "[${typeParameter.name}: ${typeParameter.bounds.joinToString { it.canonicalName }}]"

    fun typeParameter(typeParameter: SirTypeParameter): TypeParameterUsageSirType =
        TypeParameterUsageSirType(typeParameter, this)

    override fun toSwiftPoetTypeName(): TypeName =
        parentScope?.let { TypeVariableName(it.toSwiftPoetTypeName().name + "." + typeParameter.name) }
            ?: TypeVariableName(typeParameter.name)
}
