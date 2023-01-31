@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createSecondaryConstructor
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.getNamespace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue

internal class ConstructorsDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
    private val sharedCounter: SharedCounter,
) : BaseDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration) {

    override fun generate() {
        descriptorProvider.allSupportedClasses.forEach { classDescriptor ->
            classDescriptor.allSupportedConstructors.forEach {
                generateOverloads(it, classDescriptor)
            }
        }
    }

    private val DescriptorProvider.allSupportedClasses: List<ClassDescriptor>
        get() = this.exposedClasses.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = this.kind == ClassKind.CLASS && !this.belongsToSkieRuntime

    private val ClassDescriptor.allSupportedConstructors: List<ClassConstructorDescriptor>
        get() = descriptorProvider.getExposedConstructors(this)
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }

    private fun generateOverloads(constructor: ClassConstructorDescriptor, classDescriptor: ClassDescriptor) {
        constructor.forEachDefaultArgumentOverload { overloadParameters ->
            if (overloadParameters.isNotEmpty() || classDescriptor.generateOverloadWithNoParameters) {
                generateOverload(constructor, overloadParameters)
            }
        }
    }

    private val ClassDescriptor.generateOverloadWithNoParameters: Boolean
        get() = descriptorProvider.getExposedConstructors(this)
            .count { it.hasNoParametersIgnoringDefaultArguments } == 1

    private val ClassConstructorDescriptor.hasNoParametersIgnoringDefaultArguments: Boolean
        get() = this.valueParameters.count { !it.hasDefaultValue() } == 0

    private fun generateOverload(constructor: ClassConstructorDescriptor, parameters: List<ValueParameterDescriptor>) {
        val overload = generateOverloadWithUniqueName(constructor, parameters)

        fixOverloadLastParameterName(overload)
        removeConflictingOverloads(overload, constructor)
    }

    private fun generateOverloadWithUniqueName(
        constructor: ClassConstructorDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ): FunctionDescriptor {
        val overloadId = sharedCounter.next()

        return declarationBuilder.createSecondaryConstructor(
            name = "<init$uniqueNameSubstring$overloadId>",
            namespace = declarationBuilder.getNamespace(constructor),
            annotations = constructor.annotations,
        ) {
            valueParameters = parameters.copyWithoutDefaultValue(descriptor).withUniqueLastParameter(overloadId)
            body = { overloadIr ->
                getOverloadBody(constructor, overloadIr)
            }
        }
    }

    private fun List<ValueParameterDescriptor>.withUniqueLastParameter(id: Int): List<ValueParameterDescriptor> {
        val lastParameter = this.lastOrNull() ?: return this

        val uniqueLastParameter = lastParameter.copy(
            newOwner = lastParameter.containingDeclaration,
            newName = Name.identifier("${lastParameter.name.identifier}${uniqueNameSubstring}$id"),
            newIndex = lastParameter.index,
        )

        return this.dropLast(1) + uniqueLastParameter
    }

    override fun IrFunction.indexOfValueParameterByName(name: Name): Int {
        val searchedIdentifier = name.identifier.dropUniqueParameterMangling()

        return this.valueParameters.indexOfFirst { it.name.identifier == searchedIdentifier }
    }

    private fun String.dropUniqueParameterMangling(): String =
        this.split(uniqueNameSubstring).first()

    context(IrPluginContext, DeclarationIrBuilder)
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun getOverloadBody(
        originalConstructor: ClassConstructorDescriptor, overloadIr: IrConstructor,
    ): IrBody {
        val originalConstructorSymbol = symbolTable.referenceConstructor(originalConstructor)

        return irBlockBody {
            +irDelegatingConstructorCall(originalConstructorSymbol.owner).apply {
                passArgumentsWithMatchingNames(overloadIr)
            }
        }
    }

    private fun fixOverloadLastParameterName(overloadDescriptor: FunctionDescriptor) {
        skieContext.module.configure {
            val lastParameter = overloadDescriptor.swiftModel.parameters.lastOrNull() ?: return@configure

            lastParameter.argumentLabel = lastParameter.argumentLabel.dropUniqueParameterMangling()
        }
    }

    private fun removeConflictingOverloads(overloadDescriptor: FunctionDescriptor, constructor: ClassConstructorDescriptor) {
        skieContext.module.configure {
            val numberOfDefaultArguments = constructor.valueParameters.size - overloadDescriptor.valueParameters.size

            overloadDescriptor.swiftModel.collisionResolutionStrategy = CollisionResolutionStrategy.Remove(numberOfDefaultArguments)
        }
    }
}
