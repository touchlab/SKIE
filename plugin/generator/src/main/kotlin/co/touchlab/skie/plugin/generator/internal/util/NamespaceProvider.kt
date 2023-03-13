package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.SwiftFqName
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeSpec

internal class NamespaceProvider(
    private val module: SkieModule,
) {

    private val swiftGenNamespace: SwiftFqName.Local.TopLevel = SwiftFqName.Local.TopLevel("__SwiftGen")

    private fun withFileBuilder(block: context(SwiftModelScope) FileSpec.Builder.() -> Unit) {
        module.file(swiftGenNamespace.name, contents = block)
    }

    private val existingNamespaces = mutableSetOf<SwiftFqName>()

    init {
        addSingleNamespace(swiftGenNamespace)
    }

    fun addNamespaceFor(fqName: SwiftFqName): SwiftFqName.Local {
        val namespace = if (fqName.root == swiftGenNamespace) {
            // If someone already nests their type into our top level namespace, we don't want to nest it again.
            fqName as SwiftFqName.Local
        } else {
            swiftGenNamespace.nested(fqName)
        }
        namespace.components.forEach { component ->
            addSingleNamespace(component)
        }
        return namespace
    }

    private fun addSingleNamespace(namespace: SwiftFqName.Local) {
        if (namespace in existingNamespaces) {
            return
        }
        existingNamespaces.add(namespace)

        when (namespace) {
            is SwiftFqName.Local.TopLevel -> addTopLevelNamespace(namespace)
            is SwiftFqName.Local.Nested -> addNestedNamespace(namespace)
        }
    }

    private fun addTopLevelNamespace(namespace: SwiftFqName.Local.TopLevel) {
        withFileBuilder {
            addType(
                TypeSpec.enumBuilder(namespace.toSwiftPoetName())
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
        }
    }

    private fun addNestedNamespace(namespace: SwiftFqName.Local.Nested) {
        withFileBuilder {
            addExtension(
                ExtensionSpec.builder(namespace.parent.toSwiftPoetName())
                    .addModifiers(Modifier.PUBLIC)
                    .addType(
                        TypeSpec.enumBuilder(namespace.name)
                            .build()
                    )
                    .build()
            )
        }
    }
}
