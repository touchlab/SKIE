package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind

val ClassDescriptor.isInterface: Boolean
    get() = (this.kind == ClassKind.INTERFACE)
