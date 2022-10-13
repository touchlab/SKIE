package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class ObjcExportedInterfaceReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface") {

    val generatedClasses by declaredProperty<Set<ClassDescriptor>>()

    private val mapper by declaredProperty<Any>()

    val reflectedMapper: ObjCExportMapperReflector
        get() = mapper.reflectedBy()
}