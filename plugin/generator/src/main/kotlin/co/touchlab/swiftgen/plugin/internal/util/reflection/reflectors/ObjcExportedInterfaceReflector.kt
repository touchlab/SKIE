package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

internal class ObjcExportedInterfaceReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface") {

    val generatedClasses by declaredProperty<Set<ClassDescriptor>>()

    val categoryMembers by declaredProperty<Map<ClassDescriptor, List<CallableMemberDescriptor>>>()

    val topLevel by declaredProperty<Map<SourceFile, List<CallableMemberDescriptor>>>()

    private val mapper by declaredProperty<Any>()

    val reflectedMapper: ObjCExportMapperReflector
        get() = mapper.reflectedBy()
}
