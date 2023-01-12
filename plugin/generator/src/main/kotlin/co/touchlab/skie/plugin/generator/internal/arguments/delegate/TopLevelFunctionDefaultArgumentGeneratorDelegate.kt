package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.arguments.collision.CollisionDetector
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class TopLevelFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    descriptorProvider: NativeDescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
    collisionDetector: CollisionDetector,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(
    skieContext = skieContext,
    descriptorProvider = descriptorProvider,
    declarationBuilder = declarationBuilder,
    configuration = configuration,
    collisionDetector = collisionDetector,
    sharedCounter = sharedCounter,
) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exportedTopLevelCallableDescriptors
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty() && !this.belongsToSkieRuntime
}
