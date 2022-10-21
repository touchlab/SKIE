package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.Generator
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftpack.api.SkieContext

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
