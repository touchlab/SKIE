package co.touchlab.skie.phases.features.defaultarguments

import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.features.defaultarguments.delegate.ClassMethodsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ConstructorsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ExtensionFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.TopLevelFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.util.SharedCounter
import co.touchlab.skie.phases.SkieCompilationPhase
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

internal class DefaultArgumentGenerator(
    descriptorProvider: DescriptorProvider,
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
