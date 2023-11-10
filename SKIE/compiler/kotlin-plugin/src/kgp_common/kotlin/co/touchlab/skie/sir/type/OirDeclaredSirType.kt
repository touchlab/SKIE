package co.touchlab.skie.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeParameter

data class OirDeclaredSirType(
    val declaration: OirClass,
    private val typeArguments: List<OirType> = emptyList(),
    override val pointsToInternalName: Boolean = true,
    private val mapTypeArgument: (OirType, SirTypeParameter) -> SirType,
) : DeclaredSirType() {

    override val isHashable: Boolean
        get() = declaration.primarySirClass.isHashable

    override val isReference: Boolean
        get() = declaration.primarySirClass.isReference

    override fun asHashableType(): SirType? =
        getType(declaration.primarySirClass).asHashableType()

    override fun asReferenceType(): SirType? {
        val bridgedClass = declaration.bridgedSirClass

        return when {
            bridgedClass?.isReference == true -> getType(bridgedClass)
            declaration.originalSirClass.isReference -> getType(declaration.originalSirClass)
            else -> null
        }
    }

    override fun evaluate(): EvaluatedSirType<SirDeclaredSirType> =
        getType(declaration.primarySirClass).evaluate()

    private fun getType(selectedClass: SirClass): SirDeclaredSirType {
        val convertedTypeArguments = selectedClass.typeParameters
            .mapIndexed { index, typeParameter -> typeParameter to (typeArguments.getOrNull(index) ?: SpecialReferenceOirType.Id) }
            .map { (typeParameter, typeArgument) ->
                mapTypeArgument(typeArgument, typeParameter)
            }

        return SirDeclaredSirType(
            declaration = selectedClass,
            typeArguments = convertedTypeArguments,
            pointsToInternalName = pointsToInternalName,
        )
    }

    override fun withFqName(): DeclaredSirType =
        copy(pointsToInternalName = false)

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): OirDeclaredSirType =
        this

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): OirDeclaredSirType =
        this
}
