package co.touchlab.swiftgen.plugin.internal.util

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal abstract class BaseGenerator(
    private val fileBuilderFactory: FileBuilderFactory,
    private val namespaceProvider: NamespaceProvider,
) : SwiftPackExtensionContainer {

    protected val swiftGenNamespace: DeclaredTypeName
        get() = namespaceProvider.swiftGenNamespace

    protected fun generateCode(declaration: DeclarationDescriptor, codeBuilder: FileSpec.Builder.() -> Unit) {
        fileBuilderFactory.create(declaration.kotlinName).apply(codeBuilder)
    }

    protected fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName =
        namespaceProvider.addNamespace(base, name)

    abstract fun generate(descriptorProvider: DescriptorProvider)
}