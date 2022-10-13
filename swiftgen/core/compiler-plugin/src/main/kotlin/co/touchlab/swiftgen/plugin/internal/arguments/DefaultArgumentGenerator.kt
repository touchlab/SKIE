package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.Generator
import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationBuilder
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder

internal class DefaultArgumentGenerator(
    declarationBuilder: DeclarationBuilder,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    configuration: Configuration,
) : Generator {

    private val delegates = listOf(
        ClassMethodsDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration),
        ConstructorsDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration),
    )

    override fun generate(descriptorProvider: DescriptorProvider) {
        delegates.forEach {
            it.generate(descriptorProvider)
        }
    }
}
