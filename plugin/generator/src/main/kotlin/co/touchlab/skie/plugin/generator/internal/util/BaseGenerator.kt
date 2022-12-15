package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import io.outfoxx.swiftpoet.DeclaredTypeName

internal abstract class BaseGenerator(
    private val skieContext: SkieContext,
    private val namespaceProvider: NamespaceProvider,
    override val configuration: Configuration,
) : SkieCompilationPhase, ConfigurationContainer, SwiftPoetExtensionContainer {

    protected val module: SkieModule
        get() = skieContext.module

    protected val swiftGenNamespace: DeclaredTypeName
        get() = namespaceProvider.swiftGenNamespace

    protected fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName =
        namespaceProvider.addNamespace(base, name)
}
