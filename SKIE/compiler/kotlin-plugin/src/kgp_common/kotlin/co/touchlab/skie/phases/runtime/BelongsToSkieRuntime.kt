package co.touchlab.skie.phases.runtime

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

val DeclarationDescriptor.belongsToSkieRuntime: Boolean
    get() = this.fqNameSafe.asString().startsWith("co.touchlab.skie.runtime")
