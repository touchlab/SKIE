package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.arguments.DefaultArgumentGenerator
import co.touchlab.skie.plugin.generator.internal.coroutines.suspend.SuspendGenerator
import co.touchlab.skie.plugin.generator.internal.datastruct.DataStructGenerator
import co.touchlab.skie.plugin.generator.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.plugin.generator.internal.runtime.KotlinRuntimeHidingPhase
import co.touchlab.skie.plugin.generator.internal.runtime.SwiftRuntimeGenerator
import co.touchlab.skie.plugin.generator.internal.sealed.SealedInteropGenerator
import co.touchlab.skie.plugin.generator.internal.`typealias`.TypeAliasGenerator
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

    private val compilationPhases = listOf(
        IrValidator(
            reporter = reporter,
            configuration = configuration
        ),
        KotlinRuntimeHidingPhase(
            skieContext = skieContext,
        ),
        SwiftRuntimeGenerator(
            skieContext = skieContext,
            configuration = configuration
        ),
        SealedInteropGenerator(
            skieContext = skieContext,
            namespaceProvider = namespaceProvider,
            configuration = configuration,
            reporter = reporter,
        ),
        DefaultArgumentGenerator(
            skieContext = skieContext,
            declarationBuilder = declarationBuilder,
            configuration = configuration,
        ),
        ExhaustiveEnumsGenerator(
            skieContext = skieContext,
            namespaceProvider = namespaceProvider,
            configuration = configuration,
            reporter = reporter,
        ),
        SuspendGenerator(
            skieContext = skieContext,
            namespaceProvider = namespaceProvider,
            configuration = configuration,
            declarationBuilder = declarationBuilder,
        ),
        DataStructGenerator(
            skieContext = skieContext,
            namespaceProvider = namespaceProvider,
            configuration = configuration,
            reporter = reporter,
        ),
        TypeAliasGenerator(
            skieContext = skieContext,
            configuration = configuration,
        ),
    )

    fun process(descriptorProvider: DescriptorProvider) {
        compilationPhases
            .filter { it.isActive }
            .forEach { it.execute(descriptorProvider) }
    }
}
