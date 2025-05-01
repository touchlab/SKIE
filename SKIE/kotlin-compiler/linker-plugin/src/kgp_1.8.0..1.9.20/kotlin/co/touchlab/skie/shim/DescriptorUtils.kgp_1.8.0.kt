package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor

actual fun DeclarationDescriptor.findPackage(): PackageFragmentDescriptor = findPackage()
