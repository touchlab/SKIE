package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.model.type.stableSpec
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer.Companion.TYPE_VARIABLE_BASE_BOUND_NAME
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

internal interface SealedGeneratorExtensionContainer : ConfigurationContainer, SwiftPoetExtensionContainer {

    val KotlinClassSwiftModel.elseCaseName: String
        get() = this.getConfiguration(SealedInterop.ElseName)

    fun KotlinClassSwiftModel.enumCaseName(preferredNamesCollide: Boolean): String =
        if (preferredNamesCollide) this.enumCaseNameBasedOnSwiftIdentifier else this.enumCaseNameBasedOnKotlinIdentifier

    val KotlinClassSwiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide: Boolean
        get() {
            val names = this.visibleSealedSubclasses.map { it.enumCaseNameBasedOnKotlinIdentifier }

            return names.size != names.distinct().size
        }

    val KotlinClassSwiftModel.enumCaseNameBasedOnKotlinIdentifier: String
        get() {
            val configuredName = this.getConfiguration(SealedInterop.Case.Name)

            return configuredName ?: this.classDescriptor.name.identifier
        }

    val KotlinClassSwiftModel.enumCaseNameBasedOnSwiftIdentifier: String
        get() {
            val configuredName = this.getConfiguration(SealedInterop.Case.Name)

            return configuredName ?: this.fqName.toValidSwiftIdentifier()
        }

    val KotlinClassSwiftModel.hasElseCase: Boolean
        get() = this.hasUnexposedSealedSubclasses ||
            this.exposedSealedSubclasses.size != this.visibleSealedSubclasses.size ||
            this.visibleSealedSubclasses.isEmpty()

    val KotlinClassSwiftModel.visibleSealedSubclasses: List<KotlinClassSwiftModel>
        get() = this.exposedSealedSubclasses.filter { it.getConfiguration(SealedInterop.Case.Visible) }

    fun KotlinClassSwiftModel.swiftNameWithTypeParametersForSealedCase(parent: KotlinClassSwiftModel): TypeName {
        if (kind.isInterface) {
            return this.stableSpec
        }

        val typeParameters = this.classDescriptor.declaredTypeParameters.map {
            val indexInParent = it.indexInParent(this.classDescriptor, parent.classDescriptor)

            if (indexInParent != null) {
                parent.classDescriptor.declaredTypeParameters[indexInParent].swiftName
            } else {
                TYPE_VARIABLE_BASE_BOUND_NAME
            }
        }

        return this.stableSpec.withTypeParameters(typeParameters)
    }

    private fun TypeParameterDescriptor.indexInParent(child: ClassDescriptor, parent: ClassDescriptor): Int? {
        if (parent.kind.isInterface) {
            return null
        }

        val parentType = child.typeConstructor.supertypes
            .firstOrNull { it.constructor.declarationDescriptor == parent }
            ?: throw IllegalArgumentException("$parent is not a parent of $this.")

        val index = parentType.arguments.indexOfFirst { it.type == this.defaultType }

        return if (index != -1) index else null
    }
}
