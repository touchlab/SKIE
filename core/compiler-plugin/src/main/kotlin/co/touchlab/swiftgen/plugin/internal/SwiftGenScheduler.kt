package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.swiftgen.plugin.internal.sealed.SealedInteropGenerator
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.validation.IrValidator
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

internal class SwiftGenScheduler(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: SwiftGenConfiguration,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    reporter: Reporter,
) {

    private val irValidator = IrValidator(reporter)

    private val exhaustiveEnumsGenerator = ExhaustiveEnumsGenerator(fileBuilderFactory, namespaceProvider, swiftPackModuleBuilder)

    private val sealedInteropGenerator = SealedInteropGenerator(
        fileBuilderFactory = fileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration.sealedInteropDefaults,
        swiftPackModuleBuilder = swiftPackModuleBuilder,
        reporter = reporter,
    )

    fun process(module: ModuleDescriptor) {
        irValidator.validate(module)
        sealedInteropGenerator.generate(module)
        exhaustiveEnumsGenerator.generate(module)
    }
}