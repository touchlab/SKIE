@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.module

val ModuleDescriptor.isSkieKotlinRuntime: Boolean
    get() = (stableName ?: name).asString().lowercase().let { it.contains("co.touchlab.skie") && it.contains("runtime") }

val DeclarationDescriptor.belongsToSkieKotlinRuntime: Boolean
    get() = module.isSkieKotlinRuntime
