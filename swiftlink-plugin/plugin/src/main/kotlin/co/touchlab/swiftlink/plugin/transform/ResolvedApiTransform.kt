package co.touchlab.swiftlink.plugin.transform

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

sealed interface ResolvedApiTransform {
    val target: Target

    sealed interface CallableMemberTransform: ResolvedApiTransform

    sealed interface CallableMemberParentTransform: ResolvedApiTransform {
        override val target: Target.CallableMemberParent
        val classOrProtocolName: ObjCExportNamer.ClassOrProtocolName
        val bridgedName: BridgedName?
        val hide: Boolean
        val remove: Boolean
        val newSwiftName: ResolvedName?
    }

    data class PropertyTransform(
        override val target: Target.Property,
        val name: String,
        val newSwiftName: String?,
        val hide: Boolean,
        val remove: Boolean,
        val isStatic: Boolean,
    ): CallableMemberTransform

    data class FunctionTransform(
        override val target: Target.Function,
        val selector: String,
        val newSwiftSelector: String?,
        val hide: Boolean,
        val remove: Boolean,
        val isStatic: Boolean,
    ): CallableMemberTransform

    data class TypeTransform(
        override val target: Target.Type,
        val isProtocol: Boolean,
        override val classOrProtocolName: ObjCExportNamer.ClassOrProtocolName,
        override val newSwiftName: ResolvedName?,
        override val bridgedName: BridgedName?,
        override val hide: Boolean,
        override val remove: Boolean,
    ): CallableMemberParentTransform

    data class FileTransform(
        override val target: Target.File,
        override val classOrProtocolName: ObjCExportNamer.ClassOrProtocolName,
        override val newSwiftName: ResolvedName?,
        override val bridgedName: BridgedName?,
        override val hide: Boolean,
        override val remove: Boolean,
    ): CallableMemberParentTransform

    sealed interface Target {
        sealed interface CallableMemberParent: Target

        data class Property(val descriptor: PropertyDescriptor): Target
        data class Function(val descriptor: FunctionDescriptor): Target
        data class Type(val descriptor: ClassDescriptor): Target, CallableMemberParent
        data class File(val packageDescriptor: PackageFragmentDescriptor, val file: SourceFile): Target, CallableMemberParent
    }
}
