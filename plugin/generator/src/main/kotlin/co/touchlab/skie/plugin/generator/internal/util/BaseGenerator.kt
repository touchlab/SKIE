package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer

internal abstract class BaseGenerator(
    override val skieContext: SkieContext,
    private val namespaceProvider: NamespaceProvider,
) : SkieCompilationPhase, ConfigurationContainer, SwiftPoetExtensionContainer {

    protected val module: SkieModule
        get() = skieContext.module

    protected fun addNamespaceFor(name: SwiftFqName): SwiftFqName.Local =
        namespaceProvider.addNamespaceFor(name)
}
