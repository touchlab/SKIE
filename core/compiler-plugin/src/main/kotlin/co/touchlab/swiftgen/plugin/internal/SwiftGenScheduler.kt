package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.swiftgen.plugin.internal.sealed.SealedInteropGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.IrValidator
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder

internal class SwiftGenScheduler(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    reporter: Reporter,
) {

    private val irValidator = IrValidator(reporter, configuration)

    private val exhaustiveEnumsGenerator = ExhaustiveEnumsGenerator(
        fileBuilderFactory = fileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        swiftPackModuleBuilder = swiftPackModuleBuilder,
    )

    private val sealedInteropGenerator = SealedInteropGenerator(
        fileBuilderFactory = fileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration,
        swiftPackModuleBuilder = swiftPackModuleBuilder,
        reporter = reporter,
    )

    fun process(descriptorProvider: DescriptorProvider) {
        irValidator.validate(descriptorProvider)
        sealedInteropGenerator.generate(descriptorProvider)
        exhaustiveEnumsGenerator.generate(descriptorProvider)
    }
}