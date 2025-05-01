package co.touchlab.skie.shim

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.findPackage

actual fun DeclarationDescriptor.findPackage(): PackageFragmentDescriptor = findPackage()
