package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class ClassMethodsDefaultArgumentGeneratorDelegate(
    declarationBuilder: DeclarationBuilder,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    configuration: Configuration,
) : BaseFunctionDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.allSupportedClasses().flatMap { classDescriptor ->
            classDescriptor.allSupportedMethods()
        }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.classDescriptors.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = when (this.kind) {
            ClassKind.CLASS, ClassKind.ENUM_CLASS, ClassKind.OBJECT -> true
            ClassKind.INTERFACE, ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> false
        }

    private fun ClassDescriptor.allSupportedMethods(): List<SimpleFunctionDescriptor> =
        this.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.dispatchReceiverParameter != null &&
                this.extensionReceiverParameter == null &&
                this.contextReceiverParameters.isEmpty()
}
