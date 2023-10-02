package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.ConfigurationProvider
import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.superClassType
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

interface SealedGeneratorExtensionContainer {

    val context: SirPhase.Context

    val configurationProvider: ConfigurationProvider
        get() = context.configurationProvider

    val KotlinClassSwiftModel.elseCaseName: String
        get() = configurationProvider.getConfiguration(this, SealedInterop.ElseName)

    fun KotlinClassSwiftModel.enumCaseName(preferredNamesCollide: Boolean): String =
        if (preferredNamesCollide) this.enumCaseNameBasedOnSwiftIdentifier else this.enumCaseNameBasedOnKotlinIdentifier

    val KotlinClassSwiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide: Boolean
        get() {
            val names = this.visibleSealedSubclasses.map { it.enumCaseNameBasedOnKotlinIdentifier }

            return names.size != names.distinct().size
        }

    val KotlinClassSwiftModel.enumCaseNameBasedOnKotlinIdentifier: String
        get() {
            val configuredName = configurationProvider.getConfiguration(this, SealedInterop.Case.Name)

            return configuredName ?: this.classDescriptor.name.identifier.replaceFirstChar { it.lowercase() }.toValidSwiftIdentifier()
        }

    val KotlinClassSwiftModel.enumCaseNameBasedOnSwiftIdentifier: String
        get() {
            val configuredName = configurationProvider.getConfiguration(this, SealedInterop.Case.Name)

            return configuredName ?: this.kotlinSirClass.fqName.toLocalString().toValidSwiftIdentifier()
        }

    val KotlinClassSwiftModel.hasElseCase: Boolean
        get() = this.hasUnexposedSealedSubclasses ||
                this.exposedSealedSubclasses.size != this.visibleSealedSubclasses.size ||
                this.visibleSealedSubclasses.isEmpty()

    val KotlinClassSwiftModel.visibleSealedSubclasses: List<KotlinClassSwiftModel>
        get() = this.exposedSealedSubclasses.filter { configurationProvider.getConfiguration(it, SealedInterop.Case.Visible) }

    fun SirClass.getSealedSubclassType(
        enum: SirClass,
        swiftModelScope: SwiftModelScope,
    ): SirType = DeclaredSirType(
        declaration = this,
        typeArguments = this.getTypeArgumentsForEnumCase(enum, swiftModelScope),
    )

    private fun SirClass.getTypeArgumentsForEnumCase(
        enum: SirClass,
        swiftModelScope: SwiftModelScope,
    ): List<SirType> {
        val superClassTypeArguments = superClassType?.typeArguments

        return typeParameters.map { typeParameter ->
            val indexOfParentTypeParameter = superClassTypeArguments?.indexOfFirst {
                it is TypeParameterUsageSirType && it.typeParameter == typeParameter
            } ?: -1

            enum.typeParameters.getOrNull(indexOfParentTypeParameter)?.toTypeParameterUsage()
                ?: swiftModelScope.sirBuiltins.Swift.AnyObject.defaultType
        }
    }
}
