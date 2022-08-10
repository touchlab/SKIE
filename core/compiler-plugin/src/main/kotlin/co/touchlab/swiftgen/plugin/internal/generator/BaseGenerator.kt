package co.touchlab.swiftgen.plugin.internal.generator

import co.touchlab.swiftgen.plugin.internal.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.NamespaceProvider
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal abstract class BaseGenerator<T>(
    private val fileBuilderFactory: FileBuilderFactory,
    private val namespaceProvider: NamespaceProvider,
) {

    val swiftGenNamespace: DeclaredTypeName
        get() = namespaceProvider.swiftGenNamespace

    protected fun generateCode(declaration: IrDeclarationParent, codeBuilder: FileSpec.Builder.() -> Unit) {
        val kotlinName = declaration.kotlinFqName.asString()

        fileBuilderFactory.create(kotlinName).apply(codeBuilder)
    }

    protected fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName =
        namespaceProvider.addNamespace(base, name)

    abstract fun generate(declaration: T)
}