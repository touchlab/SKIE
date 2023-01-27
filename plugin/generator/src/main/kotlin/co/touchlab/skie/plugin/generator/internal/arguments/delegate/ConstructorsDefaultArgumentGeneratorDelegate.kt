@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.arguments.collision.CollisionDetector
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
import org.jetbrains.kotlin.ir.expressions.IrBody

internal class ConstructorsDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
    collisionDetector: CollisionDetector,
    private val sharedCounter: SharedCounter,
) : BaseDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration, collisionDetector) {

    override fun generate() {
        descriptorProvider.allSupportedClasses().forEach { classDescriptor ->
            classDescriptor.allSupportedConstructors(descriptorProvider).forEach {
                generateOverloads(it)
            }
        }
    }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.exposedClasses.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = this.kind == ClassKind.CLASS && !this.belongsToSkieRuntime

    private fun ClassDescriptor.allSupportedConstructors(descriptorProvider: DescriptorProvider): List<ClassConstructorDescriptor> =
        this.constructors
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }
            .filter { descriptorProvider.isExposed(it) }

    private fun generateOverloads(constructor: ClassConstructorDescriptor) {
        constructor.forEachNonCollidingDefaultArgumentOverload { overloadParameters ->
            generateOverload(constructor, overloadParameters)
        }
    }

    private fun generateOverload(
        constructor: ClassConstructorDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ): FunctionDescriptor =
        declarationBuilder.createSecondaryConstructor(
            name = "<init__Skie_DefaultArguments__${sharedCounter.next()}>",
            namespace = declarationBuilder.getNamespace(constructor),
            annotations = constructor.annotations,
        ) {
            valueParameters = parameters.copyWithoutDefaultValue(descriptor)
            body = { overloadIr ->
                getOverloadBody(constructor, overloadIr)
            }
        }

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
}
