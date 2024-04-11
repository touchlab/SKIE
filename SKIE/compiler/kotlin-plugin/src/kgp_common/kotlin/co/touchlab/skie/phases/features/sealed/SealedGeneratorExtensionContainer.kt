package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.classDescriptorOrError
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.superClassType
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

interface SealedGeneratorExtensionContainer {

    val context: SirPhase.Context

    val KirClass.elseCaseName: String
        get() = this.configuration[SealedInterop.ElseName]

    fun KirClass.enumCaseName(preferredNamesCollide: Boolean): String =
        if (preferredNamesCollide) this.enumCaseNameBasedOnSwiftIdentifier else this.enumCaseNameBasedOnKotlinIdentifier

    val KirClass.enumCaseNamesBasedOnKotlinIdentifiersCollide: Boolean
        get() {
            val names = this.visibleSealedSubclasses.map { it.enumCaseNameBasedOnKotlinIdentifier }

            return names.size != names.distinct().size
        }

    val KirClass.enumCaseNameBasedOnKotlinIdentifier: String
        get() {
            val configuredName = this.configuration[SealedInterop.Case.Name]

            return configuredName ?: classDescriptorOrError.name.identifier.replaceFirstChar { it.lowercase() }.toValidSwiftIdentifier()
        }

    val KirClass.enumCaseNameBasedOnSwiftIdentifier: String
        get() {
            val configuredName = this.configuration[SealedInterop.Case.Name]

            return configuredName ?: this.originalSirClass.publicName.toLocalString().toValidSwiftIdentifier()
        }

    val KirClass.hasElseCase: Boolean
        get() = this.hasUnexposedSealedSubclasses ||
            this.sealedSubclasses.size != this.visibleSealedSubclasses.size ||
            this.visibleSealedSubclasses.isEmpty()

    val KirClass.visibleSealedSubclasses: List<KirClass>
        get() = this.sealedSubclasses.filter { it.configuration[SealedInterop.Case.Visible] }

    fun SirClass.getSealedSubclassType(
        enum: SirClass,
    ): SirType = SirDeclaredSirType(
        declaration = this,
        typeArguments = this.getTypeArgumentsForEnumCase(enum),
    )

    private fun SirClass.getTypeArgumentsForEnumCase(
        enum: SirClass,
    ): List<SirType> {
        val superClassTypeArguments = superClassType?.typeArguments

        return typeParameters.map { typeParameter ->
            val indexOfParentTypeParameter = superClassTypeArguments?.indexOfFirst {
                it is TypeParameterUsageSirType && it.typeParameter == typeParameter
            } ?: -1

            enum.typeParameters.getOrNull(indexOfParentTypeParameter)?.toTypeParameterUsage()
                ?: context.sirBuiltins.Swift.AnyObject.defaultType
        }
    }
}
