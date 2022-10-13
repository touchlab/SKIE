package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.createSecondaryConstructor
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

internal class ConstructorsDefaultArgumentGeneratorDelegate(
    declarationBuilder: DeclarationBuilder,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    configuration: Configuration,
) : BaseDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedClasses().forEach { classDescriptor ->
            classDescriptor.allSupportedConstructors(descriptorProvider).forEach { constructorDescriptor ->
                generateOverloads(constructorDescriptor, classDescriptor)
            }
        }
    }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.classDescriptors.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = this.kind == ClassKind.CLASS

    private fun ClassDescriptor.allSupportedConstructors(descriptorProvider: DescriptorProvider): List<ClassConstructorDescriptor> =
        this.constructors
            .filter { it.hasDefaultArguments }
            .filter { descriptorProvider.shouldBeExposed(it) }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private fun generateOverloads(constructor: ClassConstructorDescriptor, parentClass: ClassDescriptor) {
        constructor.forEachDefaultArgumentOverload { index, overloadParameters ->
            generateOverload(constructor, parentClass, index, overloadParameters)
        }
    }

    private fun generateOverload(
        constructor: ClassConstructorDescriptor,
        parentClass: ClassDescriptor,
        index: Int,
        parameters: List<ValueParameterDescriptor>,
    ) {
        declarationBuilder.createSecondaryConstructor(
            name = "<init__SwiftGen__${index}>",
            namespace = declarationBuilder.getNamespace(parentClass),
            annotations = constructor.annotations,
        ) {
            valueParameters = parameters.copyWithoutDefaultValue(descriptor)
            body = { overloadIr ->
                getOverloadBody(constructor, overloadIr)
            }
        }
    }

    context(ReferenceSymbolTable, DeclarationIrBuilder) private fun getOverloadBody(
        originalConstructor: ClassConstructorDescriptor, overloadIr: IrConstructor,
    ): IrBody {
        val originalConstructorSymbol = referenceConstructor(originalConstructor)

        return irBlockBody {
            +irDelegatingConstructorCall(originalConstructorSymbol.owner).apply {
                passArgumentsWithMatchingNames(overloadIr)
            }
        }
    }
}
