package co.touchlab.skie.shim

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor

expect fun DeclarationDescriptor.findPackage(): PackageFragmentDescriptor
