package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.irbuilder.createSecondaryConstructor
import co.touchlab.skie.kir.irbuilder.getNamespace
import co.touchlab.skie.kir.irbuilder.util.copyWithoutDefaultValue
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.descriptorKirProvider
import co.touchlab.skie.phases.features.defaultarguments.DefaultArgumentGenerator
import co.touchlab.skie.phases.skieSymbolTable
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.util.SharedCounter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue

class ConstructorsDefaultArgumentGeneratorDelegate(
    context: DescriptorModificationPhase.Context,
    private val sharedCounter: SharedCounter,
) : BaseDefaultArgumentGeneratorDelegate(context) {

    context(DescriptorModificationPhase.Context)
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
        get() = this.kind == ClassKind.CLASS

    context(DescriptorModificationPhase.Context)
    private val ClassDescriptor.allSupportedConstructors: List<ClassConstructorDescriptor>
        get() = descriptorProvider.getExposedConstructors(this)
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }

    context(DescriptorModificationPhase.Context)
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

    context(DescriptorModificationPhase.Context)
    private fun generateOverload(constructor: ClassConstructorDescriptor, parameters: List<ValueParameterDescriptor>) {
        val overload = generateOverloadWithUniqueName(constructor, parameters)

        registerOverload(overload, constructor)
    }

    context(DescriptorModificationPhase.Context)
    private fun generateOverloadWithUniqueName(
        constructor: ClassConstructorDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ): ClassConstructorDescriptor {
        val overloadId = sharedCounter.next()

        val overload = declarationBuilder.createSecondaryConstructor(
            name = "<init$uniqueNameSubstring$overloadId>",
            namespace = declarationBuilder.getNamespace(constructor),
            annotations = constructor.annotations,
        ) {
            descriptor.configuration.overwriteBy(constructor.configuration)

            valueParameters = parameters.copyWithoutDefaultValue(descriptor).withUniqueLastParameter(overloadId)
            body = { overloadIr ->
                getOverloadBody(constructor, overloadIr)
            }
        }

        if (parameters.isNotEmpty()) {
            removeManglingOfOverload(overload.valueParameters.last(), parameters.last())
        }

        return overload
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

    context(KotlinIrPhase.Context, DeclarationIrBuilder)
    private fun getOverloadBody(
        originalConstructor: ClassConstructorDescriptor, overloadIr: IrConstructor,
    ): IrBody {
        val originalConstructorSymbol = skieSymbolTable.descriptorExtension.referenceConstructor(originalConstructor)

        return irBlockBody {
            +irDelegatingConstructorCall(originalConstructorSymbol.owner).apply {
                passDispatchReceiverParameterIfPresent(overloadIr)
                passArgumentsWithMatchingNames(overloadIr)
            }
        }
    }

    context(IrBuilderWithScope)
    private fun IrDelegatingConstructorCall.passDispatchReceiverParameterIfPresent(from: IrFunction) {
        val dispatchReceiverParameter = from.dispatchReceiverParameter ?: return

        this.dispatchReceiver = irGet(dispatchReceiverParameter)
    }

    private fun registerOverload(overloadDescriptor: ClassConstructorDescriptor, constructor: ClassConstructorDescriptor) {
        context.doInPhase(DefaultArgumentGenerator.RegisterOverloadsPhase) {
            val overloadKirConstructor = descriptorKirProvider.getConstructor(overloadDescriptor)

            descriptorKirProvider.getConstructor(constructor).defaultArgumentsOverloads.add(overloadKirConstructor)
        }
    }

    private fun removeManglingOfOverload(
        mangledValueParameterDescriptorFromOverload: ValueParameterDescriptor,
        mangledValueParameterDescriptorFromConstructor: ValueParameterDescriptor,
    ) {
        context.doInPhase(DefaultArgumentGenerator.RemoveManglingOfOverloadsInitPhase) {
            val mangledValueParameterFromOverload = descriptorKirProvider.getValueParameter(mangledValueParameterDescriptorFromOverload)
            val mangledValueParameterFromConstructor = descriptorKirProvider.getValueParameter(mangledValueParameterDescriptorFromConstructor)

            doInPhase(DefaultArgumentGenerator.RemoveManglingOfOverloadsFinalizePhase) {
                val sirMangledValueParameterFromOverload = mangledValueParameterFromOverload
                    .oirValueParameter
                    .originalSirValueParameter

                val sirMangledValueParameterFromConstructor = mangledValueParameterFromConstructor
                    .oirValueParameter
                    .originalSirValueParameter

                sirMangledValueParameterFromOverload?.label = sirMangledValueParameterFromConstructor?.labelOrName
            }
        }
    }
}
