package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.configuration.ConfigurationContainer
import co.touchlab.swiftpack.api.SkieContext
import co.touchlab.swiftpack.api.SkieModule
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.api.SwiftPoetContext
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal abstract class BaseGenerator(
    private val skieContext: SkieContext,
    private val namespaceProvider: NamespaceProvider,
    override val configuration: Configuration,
) : Generator, ConfigurationContainer, SwiftPoetExtensionContainer {

    protected val module: SkieModule
        get() = skieContext.module

    protected val swiftGenNamespace: DeclaredTypeName
        get() = namespaceProvider.swiftGenNamespace

    protected fun generateCode(
        declaration: DeclarationDescriptor,
        codeBuilder: context(SwiftPoetContext) FileSpec.Builder.() -> Unit,
    ) {
        module.file(declaration.kotlinName, codeBuilder)
    }

    protected fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName =
        namespaceProvider.addNamespace(base, name)
}
