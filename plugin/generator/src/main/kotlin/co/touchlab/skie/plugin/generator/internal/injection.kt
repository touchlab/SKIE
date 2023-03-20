package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useImpl

fun StorageComponentContainer.registerGeneratorComponents() {
    useImpl<DeclarationBuilderImpl>()
}
