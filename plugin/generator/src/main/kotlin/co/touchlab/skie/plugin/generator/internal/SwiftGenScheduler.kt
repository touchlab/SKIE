package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.arguments.DefaultArgumentGenerator
import co.touchlab.skie.plugin.generator.internal.datastruct.DataStructGenerator
import co.touchlab.skie.plugin.generator.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.plugin.generator.internal.runtime.RuntimeGenerator
import co.touchlab.skie.plugin.generator.internal.sealed.SealedInteropGenerator
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.validation.IrValidator

internal class SwiftGenScheduler(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    reporter: Reporter,
) {

    private val irValidator = IrValidator(reporter, configuration)

    private val runtimeGenerator = RuntimeGenerator(skieContext)

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

    private val dataStructGenerator = DataStructGenerator(
        skieContext = skieContext,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        reporter = reporter,
    )

    fun process(descriptorProvider: DescriptorProvider) {
        irValidator.validate(descriptorProvider)
        runtimeGenerator.generate(descriptorProvider)
        sealedInteropGenerator.generate(descriptorProvider)
        defaultArgumentGenerator.generate(descriptorProvider)
        exhaustiveEnumsGenerator.generate(descriptorProvider)
        dataStructGenerator.generate(descriptorProvider)
    }
}
