@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.callable

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor

context(MutableSwiftModelScope)
fun CallableMemberDescriptor.getReceiverSwiftModel(namer: ObjCExportNamer): MutableKotlinTypeSwiftModel {
    val categoryClass = namer.mapper.getClassIfCategory(this)
    val containingDeclaration = this.containingDeclaration

    return when {
        categoryClass != null -> categoryClass.swiftModel
        this is PropertyAccessorDescriptor -> correspondingProperty.swiftModel.receiver
        containingDeclaration is ClassDescriptor -> containingDeclaration.swiftModel
        containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
        else -> error("Unsupported containing declaration for $this")
    }
}
