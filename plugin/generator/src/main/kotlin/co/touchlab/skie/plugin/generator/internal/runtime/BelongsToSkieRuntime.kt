package co.touchlab.skie.plugin.generator.internal.runtime

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal val DeclarationDescriptor.belongsToSkieRuntime: Boolean
    get() = this.fqNameSafe.asString().startsWith("co.touchlab.skie.runtime")

internal val KotlinClassSwiftModel.belongsToSkieRuntime: Boolean
    get() = this.classDescriptor.belongsToSkieRuntime
