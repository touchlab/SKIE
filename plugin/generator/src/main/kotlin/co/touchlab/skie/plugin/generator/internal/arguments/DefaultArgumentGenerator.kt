package co.touchlab.skie.plugin.generator.internal.arguments

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ClassMethodsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ConstructorsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ExtensionFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.TopLevelFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder

internal class DefaultArgumentGenerator(
    descriptorProvider: NativeDescriptorProvider,
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val sharedCounter = SharedCounter()

    private val delegates = listOf(
        ClassMethodsDefaultArgumentGeneratorDelegate(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            sharedCounter = sharedCounter,
        ),
        ConstructorsDefaultArgumentGeneratorDelegate(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            sharedCounter = sharedCounter
        ),
        TopLevelFunctionDefaultArgumentGeneratorDelegate(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            sharedCounter = sharedCounter
        ),
        ExtensionFunctionDefaultArgumentGeneratorDelegate(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            sharedCounter = sharedCounter
        ),
    )

    override fun runObjcPhase() {
        delegates.forEach {
            it.generate()
        }
    }
}
