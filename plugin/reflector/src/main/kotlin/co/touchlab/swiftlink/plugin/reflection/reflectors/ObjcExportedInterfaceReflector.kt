package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.Reflector
import co.touchlab.swiftlink.plugin.reflection.reflectedBy
import co.touchlab.swiftlink.plugin.reflection.reflectors.ObjCExportMapperReflector
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class ObjcExportedInterfaceReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface") {

    val generatedClasses by declaredProperty<Set<ClassDescriptor>>()

    private val mapper by declaredProperty<Any>()

    val reflectedMapper: ObjCExportMapperReflector
        get() = mapper.reflectedBy()
}
