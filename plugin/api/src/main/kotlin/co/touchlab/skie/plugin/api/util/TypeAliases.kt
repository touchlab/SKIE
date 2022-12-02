package co.touchlab.skie.plugin.api.util

import io.outfoxx.swiftpoet.DeclaredTypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

val ClassDescriptor.typeAliasSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName("__Skie." + this.typeAliasName)

val ClassDescriptor.typeAliasName: String
    get() = this.fqNameSafe.asString().replace(".", "_")
