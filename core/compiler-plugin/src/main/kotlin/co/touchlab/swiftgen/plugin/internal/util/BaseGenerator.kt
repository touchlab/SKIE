package co.touchlab.swiftgen.plugin.internal.util

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal abstract class BaseGenerator(
    private val fileBuilderFactory: FileBuilderFactory,
    private val namespaceProvider: NamespaceProvider,
) {

    protected val swiftGenNamespace: DeclaredTypeName
        get() = namespaceProvider.swiftGenNamespace

    protected fun generateCode(declaration: IrDeclarationParent, codeBuilder: FileSpec.Builder.() -> Unit) {
        val kotlinName = declaration.kotlinFqName.asString()

        fileBuilderFactory.create(kotlinName).apply(codeBuilder)
    }

    protected fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName =
        namespaceProvider.addNamespace(base, name)

    abstract fun generate(module: IrModuleFragment)
}