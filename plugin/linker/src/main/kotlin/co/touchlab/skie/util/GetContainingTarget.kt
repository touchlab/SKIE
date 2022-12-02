@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.util

import co.touchlab.skie.plugin.TransformAccumulator
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor

internal fun ObjCExportNamer.getContainingTarget(descriptor: CallableMemberDescriptor): TransformAccumulator.TypeTransformTarget {
    val categoryClass = this.mapper.getClassIfCategory(descriptor)

    if (categoryClass != null) {
        return TransformAccumulator.TypeTransformTarget.Class(categoryClass)
    }

    return when (val containingDeclaration = descriptor.containingDeclaration) {
        is ClassDescriptor -> TransformAccumulator.TypeTransformTarget.Class(containingDeclaration)
        is PackageFragmentDescriptor -> TransformAccumulator.TypeTransformTarget.File(descriptor.findSourceFile())
        else -> error("Unexpected containing declaration: $containingDeclaration")
    }
}
