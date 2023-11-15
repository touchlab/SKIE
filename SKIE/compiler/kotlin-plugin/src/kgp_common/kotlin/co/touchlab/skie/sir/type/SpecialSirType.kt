package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import io.outfoxx.swiftpoet.AnyTypeName
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeName

sealed class SpecialSirType<SELF : SirType>(
    private val typeName: TypeName,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isReference: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(): EvaluatedSirType<SELF> =
        EvaluatedSirType(
            type = this,
            isValid = true,
            canonicalName = typeName.name,
            swiftPoetTypeName = typeName,
        ) as EvaluatedSirType<SELF>

    override fun inlineTypeAliases(): SirType =
        this

    @Suppress("UNCHECKED_CAST")
    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SELF =
        this as SELF

    @Suppress("UNCHECKED_CAST")
    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SELF =
        this as SELF

    object Self : SpecialSirType<Self>(SelfTypeName.INSTANCE)

    object Any : SpecialSirType<Any>(AnyTypeName.INSTANCE)
}
