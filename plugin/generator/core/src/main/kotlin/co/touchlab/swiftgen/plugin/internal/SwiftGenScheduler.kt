package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.arguments.DefaultArgumentGenerator
import co.touchlab.swiftgen.plugin.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.swiftgen.plugin.internal.sealed.SealedInteropGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.validation.IrValidator

internal class SwiftGenScheduler(
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    declarationBuilder: DeclarationBuilder,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    reporter: Reporter,
) {

    private val irValidator = IrValidator(reporter, configuration)

    private val exhaustiveEnumsGenerator = ExhaustiveEnumsGenerator(
        swiftFileBuilderFactory = swiftFileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        reporter = reporter,
    )

    private val sealedInteropGenerator = SealedInteropGenerator(
        swiftFileBuilderFactory = swiftFileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        reporter = reporter,
    )

    private val defaultArgumentGenerator = DefaultArgumentGenerator(
        declarationBuilder = declarationBuilder,
        swiftPackModuleBuilder = swiftFileBuilderFactory.swiftPackModuleBuilder,
        configuration = configuration,
    )

    fun process(descriptorProvider: DescriptorProvider) {
        irValidator.validate(descriptorProvider)
        sealedInteropGenerator.generate(descriptorProvider)
        defaultArgumentGenerator.generate(descriptorProvider)
        exhaustiveEnumsGenerator.generate(descriptorProvider)
    }
}
