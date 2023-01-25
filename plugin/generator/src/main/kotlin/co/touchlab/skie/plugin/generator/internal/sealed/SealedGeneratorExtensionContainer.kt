package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.module.stableSpec
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer.Companion.TYPE_VARIABLE_BASE_BOUND_NAME
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

internal interface SealedGeneratorExtensionContainer : ConfigurationContainer, SwiftPoetExtensionContainer {

    val descriptorProvider: DescriptorProvider

    val ClassDescriptor.elseCaseName: String
        get() = this.getConfiguration(SealedInterop.ElseName)

    val ClassDescriptor.enumCaseName: String
        get() {
            val configuredName = this.getConfiguration(SealedInterop.Case.Name)

            return configuredName ?: this.name.identifier
        }

    val ClassDescriptor.hasElseCase: Boolean
        get() = this.sealedSubclasses.any { !it.isExplicitSealedSubclass } || this.sealedSubclasses.isEmpty()

    val ClassDescriptor.explicitSealedSubclasses: List<ClassDescriptor>
        get() = this.sealedSubclasses.filter { it.isExplicitSealedSubclass }

    val ClassDescriptor.isExplicitSealedSubclass: Boolean
        get() {
            val isVisible = descriptorProvider.isExposed(this)

            val isEnabled = this.getConfiguration(SealedInterop.Case.Visible)

            return isVisible && isEnabled
        }

    context(ClassDescriptor, SwiftModelScope)
    fun swiftNameWithTypeParametersForSealedCase(parent: ClassDescriptor): TypeName {
        if (kind.isInterface) {
            return this@ClassDescriptor.stableSpec
        }

        val typeParameters = declaredTypeParameters.map {
            val indexInParent = it.indexInParent(this@ClassDescriptor, parent)

            if (indexInParent != null) {
                parent.declaredTypeParameters[indexInParent].swiftName
            } else {
                TYPE_VARIABLE_BASE_BOUND_NAME
            }
        }

        return this@ClassDescriptor.stableSpec.withTypeParameters(typeParameters)
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
