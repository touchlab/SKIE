package co.touchlab.skie.plugin.generator.internal.runtime

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal val DeclarationDescriptor.belongsToSkieRuntime: Boolean
    get() = this.fqNameSafe.asString().startsWith("co.touchlab.skie.runtime")
