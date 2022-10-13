package co.touchlab.swiftlink.plugin.transform

import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor

val CallableMemberDescriptor.parent: ResolvedApiTransform.Target.CallableMemberParent
    get() = when (val parent = containingDeclaration) {
        is ClassDescriptor -> ResolvedApiTransform.Target.Type(parent)
        is PackageFragmentDescriptor -> ResolvedApiTransform.Target.File(parent, findSourceFile())
        else -> error("Unknown parent type: $parent")
    }
