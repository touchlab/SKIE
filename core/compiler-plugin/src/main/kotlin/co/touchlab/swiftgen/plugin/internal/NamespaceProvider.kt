package co.touchlab.swiftgen.plugin.internal

import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.safeName
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class NamespaceProvider(
    fileBuilderFactory: FileBuilderFactory,
    moduleFragment: IrModuleFragment,
) {

    val swiftGenNamespace: DeclaredTypeName =
        DeclaredTypeName.qualifiedTypeName(".__SwiftGen_${moduleFragment.safeName}")

    private val fileBuilder = fileBuilderFactory.create(swiftGenNamespace.simpleName)

    private val existingNamespaces = mutableSetOf<String>()

    init {
        addNamespace(swiftGenNamespace)
    }

    fun addNamespace(namespace: DeclaredTypeName) {
        val nameComponents = namespace.simpleNames

        val baseNamespace = DeclaredTypeName.qualifiedTypeName("${namespace.moduleName}.${nameComponents.first()}")
        addSingleNamespace(baseNamespace)

        nameComponents.drop(1).fold(baseNamespace) { acc, nameComponent ->
            addSingleNamespace(acc, nameComponent)
        }
    }

    fun addNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName {
        val knownNamespaces = base.canonicalName
            .split(".")
            .scan(emptyList(), List<String>::plus)
            .map { it.joinToString(".") }

        existingNamespaces.addAll(knownNamespaces)

        val fullNamespace = name.split(".").fold(base, DeclaredTypeName::nestedType)

        addNamespace(fullNamespace)

        return fullNamespace
    }

    private fun addSingleNamespace(namespace: DeclaredTypeName): DeclaredTypeName {
        if (namespace.canonicalName in existingNamespaces) {
            return namespace
        }
        existingNamespaces.add(namespace.canonicalName)

        fileBuilder.addType(
            TypeSpec.enumBuilder(namespace)
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        return namespace
    }

    private fun addSingleNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName {
        val namespace = base.nestedType(name)
        if (namespace.canonicalName in existingNamespaces) {
            return namespace
        }
        existingNamespaces.add(namespace.canonicalName)

        fileBuilder.addExtension(
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
}