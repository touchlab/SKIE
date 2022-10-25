package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import co.touchlab.skie.plugin.reflection.reflectedBy
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class ObjcExportedInterfaceReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface") {

    val generatedClasses by declaredProperty<Set<ClassDescriptor>>()

    private val mapper by declaredProperty<Any>()

    val reflectedMapper: ObjCExportMapperReflector
        get() = mapper.reflectedBy()
}
