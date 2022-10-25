package co.touchlab.skie.plugin.generator.internal.arguments

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Generator
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder

internal class DefaultArgumentGenerator(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : Generator {

    private val delegates = listOf(
        ClassMethodsDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        ConstructorsDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        TopLevelFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
        ExtensionFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration),
    )

    override fun generate(descriptorProvider: DescriptorProvider) {
        delegates.forEach {
            it.generate(descriptorProvider)
        }
    }
}
