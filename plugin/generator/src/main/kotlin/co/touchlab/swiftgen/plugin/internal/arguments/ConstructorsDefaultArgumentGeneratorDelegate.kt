package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.createSecondaryConstructor
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.getNamespace
import co.touchlab.swiftpack.api.SkieContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

internal class ConstructorsDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : BaseDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedClasses().forEach { classDescriptor ->
            classDescriptor.allSupportedConstructors(descriptorProvider).forEach {
                generateOverloads(it)
            }
        }
    }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.classDescriptors.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = this.kind == ClassKind.CLASS

    private fun ClassDescriptor.allSupportedConstructors(descriptorProvider: DescriptorProvider): List<ClassConstructorDescriptor> =
        this.constructors
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }
            .filter { descriptorProvider.shouldBeExposed(it) }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private fun generateOverloads(constructor: ClassConstructorDescriptor) {
        constructor.forEachDefaultArgumentOverload { index, overloadParameters ->
            generateOverload(constructor, index, overloadParameters)
        }
    }

    private fun generateOverload(
        constructor: ClassConstructorDescriptor,
        index: Int,
        parameters: List<ValueParameterDescriptor>,
    ) {
        declarationBuilder.createSecondaryConstructor(
            name = "<init__SwiftGen__${index}>",
            namespace = declarationBuilder.getNamespace(constructor),
            annotations = constructor.annotations,
        ) {
            valueParameters = parameters.copyWithoutDefaultValue(descriptor)
            body = { overloadIr ->
                getOverloadBody(constructor, overloadIr)
            }
        }
    }

    context(ReferenceSymbolTable, DeclarationIrBuilder)
        @OptIn(ObsoleteDescriptorBasedAPI::class)
        private fun getOverloadBody(
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
