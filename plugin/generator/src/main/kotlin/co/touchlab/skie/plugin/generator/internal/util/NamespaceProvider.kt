package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeSpec

internal class NamespaceProvider(
    private val module: SkieModule,
) {

    val swiftGenNamespace: DeclaredTypeName =
        DeclaredTypeName.qualifiedLocalTypeName("__SwiftGen")

    private fun withFileBuilder(block: context(SwiftPoetScope) FileSpec.Builder.() -> Unit) {
        module.file(swiftGenNamespace.simpleName, block)
    }

    private val existingNamespaces = mutableSetOf<String>()

    init {
        addNamespace(swiftGenNamespace)
    }

    fun addNamespace(namespace: DeclaredTypeName) {
        val nameComponents = namespace.simpleNames

        val baseNamespace = DeclaredTypeName.qualifiedLocalTypeName(nameComponents.first())
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

        withFileBuilder {
            addType(
                TypeSpec.enumBuilder(namespace)
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
        }

        return namespace
    }

    private fun addSingleNamespace(base: DeclaredTypeName, name: String): DeclaredTypeName {
        val namespace = base.nestedType(name)
        if (namespace.canonicalName in existingNamespaces) {
            return namespace
        }
        existingNamespaces.add(namespace.canonicalName)

        withFileBuilder {
            addExtension(
                ExtensionSpec.builder(base)
                    .addModifiers(Modifier.PUBLIC)
                    .addType(
                        TypeSpec.enumBuilder(name)
                            .build()
                    )
                    .build()
            )
        }

        return namespace
    }
}
