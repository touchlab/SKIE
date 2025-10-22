package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirVisibility
import io.outfoxx.swiftpoet.AnyTypeName
import io.outfoxx.swiftpoet.ProtocolTypeName
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeName

sealed class SpecialSirType(
    private val typeName: TypeName,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isReference: Boolean = false

    private val evaluatedSirType by lazy {
        EvaluatedSirType.Eager(
            type = this,
            canonicalName = typeName.name,
            swiftPoetTypeName = typeName,
            visibilityConstraint = SirVisibility.Public,
            referencedTypeDeclarations = emptySet(),
        )
    }

    override fun evaluate(): EvaluatedSirType = evaluatedSirType

    override fun inlineTypeAliases(): SirType =
        this

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SirType = this

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SirType = this

    object Self : SpecialSirType(SelfTypeName.INSTANCE)

    object Any : SpecialSirType(AnyTypeName.INSTANCE)

    object Protocol : SpecialSirType(ProtocolTypeName.INSTANCE) {

        override val isReference: Boolean = true

        override fun asReferenceType(): Protocol = this
    }
}
