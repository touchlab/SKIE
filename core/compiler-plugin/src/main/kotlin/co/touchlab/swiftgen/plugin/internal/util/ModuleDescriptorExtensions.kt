package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.ModuleDescriptor

internal fun ModuleDescriptor.accept(visitor: RecursiveClassDescriptorVisitor) {
    this.accept(visitor, Unit)
}
