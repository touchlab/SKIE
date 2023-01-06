@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.arguments

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.arguments.collision.CollisionDetector
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ClassMethodsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ConstructorsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.ExtensionFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.arguments.delegate.TopLevelFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder

internal class DefaultArgumentGenerator(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val delegates = listOf(
        ClassMethodsDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        ConstructorsDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        TopLevelFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        ExtensionFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
    )

    override fun execute(descriptorProvider: NativeDescriptorProvider) {
        val collisionDetector = CollisionDetector(descriptorProvider)

        delegates.forEach {
            it.generate(descriptorProvider, collisionDetector)
        }
    }
}
