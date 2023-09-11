@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

class ObjcExportedInterfaceReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface") {

    val generatedClasses by declaredProperty<Set<ClassDescriptor>>()

    val categoryMembers by declaredProperty<Map<ClassDescriptor, List<CallableMemberDescriptor>>>()

    val topLevel by declaredProperty<Map<SourceFile, List<CallableMemberDescriptor>>>()

    internal val mapper by declaredProperty<ObjCExportMapper>()

    val reflectedMapper: ObjCExportMapperReflector
        get() = mapper.reflectedBy()
}
