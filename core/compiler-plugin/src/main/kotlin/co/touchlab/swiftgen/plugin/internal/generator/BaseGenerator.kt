package co.touchlab.swiftgen.plugin.internal.generator

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.safeName
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal abstract class BaseGenerator<T>(
    private val output: SwiftPackModuleBuilder,
    moduleFragment: IrModuleFragment,
) {

    protected val swiftGenNamespace: DeclaredTypeName =
        DeclaredTypeName.qualifiedTypeName(".__SwiftGen_${moduleFragment.safeName}")

    private val existingNamespaces = mutableSetOf<String>()

    init {
        output.file("SwiftGen") {
            addNamespace(swiftGenNamespace)
        }
    }

    protected fun generateCode(declaration: IrDeclarationParent, codeBuilder: FileSpec.Builder.() -> Unit) {
        val kotlinName = declaration.kotlinFqName.asString()

        output.file(kotlinName, codeBuilder)
    }

    protected fun FileSpec.Builder.addNamespace(namespace: DeclaredTypeName) {
        val nameComponents = namespace.simpleNames

        val baseNamespace = DeclaredTypeName.qualifiedTypeName("${namespace.moduleName}.${nameComponents.first()}")
        addSingleNamespace(baseNamespace)

        nameComponents.drop(1).fold(baseNamespace) { acc, nameComponent ->
            addSingleNamespace(acc, nameComponent)
        }
    }

    protected fun FileSpec.Builder.addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName {
        val knownNamespaces = base.canonicalName
            .split(".")
            .scan(emptyList(), List<String>::plus)
            .map { it.joinToString(".") }

        existingNamespaces.addAll(knownNamespaces)

        val fullNamespace = name.split(".").fold(base, DeclaredTypeName::nestedType)

        addNamespace(fullNamespace)

        return fullNamespace
    }

    private fun FileSpec.Builder.addSingleNamespace(namespace: DeclaredTypeName): DeclaredTypeName {
        if (namespace.canonicalName in existingNamespaces) {
            return namespace
        }
        existingNamespaces.add(namespace.canonicalName)

        addType(
            TypeSpec.enumBuilder(namespace)
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        return namespace
    }

    private fun FileSpec.Builder.addSingleNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName {
        val namespace = base.nestedType(name)
        if (namespace.canonicalName in existingNamespaces) {
            return namespace
        }
        existingNamespaces.add(namespace.canonicalName)

        addExtension(
            ExtensionSpec.builder(base)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(name)
                        .build()
                )
                .build()
        )

        return namespace
    }

    abstract fun generate(declaration: T)
}