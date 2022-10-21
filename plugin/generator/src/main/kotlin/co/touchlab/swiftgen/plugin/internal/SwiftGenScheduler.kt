package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.arguments.DefaultArgumentGenerator
import co.touchlab.swiftgen.plugin.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.swiftgen.plugin.internal.sealed.SealedInteropGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.validation.IrValidator
import co.touchlab.swiftpack.api.SkieContext

internal class SwiftGenScheduler(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    reporter: Reporter,
) {

    private val irValidator = IrValidator(reporter, configuration)

    private val exhaustiveEnumsGenerator = ExhaustiveEnumsGenerator(
        skieContext = skieContext,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        reporter = reporter,
    )

    private val sealedInteropGenerator = SealedInteropGenerator(
        skieContext = skieContext,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        reporter = reporter,
    )

    private val defaultArgumentGenerator = DefaultArgumentGenerator(
        skieContext = skieContext,
        declarationBuilder = declarationBuilder,
        configuration = configuration,
    )

    fun process(descriptorProvider: DescriptorProvider) {
        irValidator.validate(descriptorProvider)
        sealedInteropGenerator.generate(descriptorProvider)
        defaultArgumentGenerator.generate(descriptorProvider)
        exhaustiveEnumsGenerator.generate(descriptorProvider)
    }
}
