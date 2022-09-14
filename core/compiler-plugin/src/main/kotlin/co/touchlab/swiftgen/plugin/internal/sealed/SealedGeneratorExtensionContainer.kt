package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.configuration.ConfigurationContainer
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration
import co.touchlab.swiftgen.plugin.internal.util.SwiftPackExtensionContainer
import co.touchlab.swiftgen.plugin.internal.util.SwiftPackExtensionContainer.Companion.TYPE_VARIABLE_BASE_BOUND_NAME
import co.touchlab.swiftgen.plugin.internal.util.isVisibleFromSwift
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

internal interface SealedGeneratorExtensionContainer : SwiftPackExtensionContainer, ConfigurationContainer {

    val ClassDescriptor.elseCaseName: String
        get() = this.getConfiguration(ConfigurationKeys.SealedInterop.ElseName)

    val ClassDescriptor.enumCaseName: String
        get() {
            val configuredName = this.getConfiguration(ConfigurationKeys.SealedInterop.Case.Name)

            return configuredName ?: this.name.identifier
        }

    val ClassDescriptor.hasElseCase: Boolean
        get() = this.sealedSubclasses.any { !it.isVisibleSealedSubclass } || this.sealedSubclasses.isEmpty()

    val ClassDescriptor.visibleSealedSubclasses: List<ClassDescriptor>
        get() = this.sealedSubclasses.filter { it.isVisibleSealedSubclass }

    val ClassDescriptor.isVisibleSealedSubclass: Boolean
        get() {
            val isVisible = this.isVisibleFromSwift

            val isEnabled = this.getConfiguration(ConfigurationKeys.SealedInterop.Case.Visible)

            return isVisible && isEnabled
        }

    fun ClassDescriptor.swiftNameWithTypeParametersForSealedCase(parent: ClassDescriptor): TypeName {
        if (this.kind.isInterface) {
            return this.swiftName
        }

        val typeParameters = this.declaredTypeParameters.map {
            val indexInParent = it.indexInParent(this, parent)

            if (indexInParent != null) {
                parent.declaredTypeParameters[indexInParent].swiftName
            } else {
                TYPE_VARIABLE_BASE_BOUND_NAME
            }
        }

        return this.swiftName.withTypeParameters(typeParameters)
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