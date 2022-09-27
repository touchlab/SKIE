package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

internal class ObjCExportMapperReflector(override val instance: Any) : Reflector(
    "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper"
) {

    private val extensionClass = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt"

    val isBaseMethod by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    val isBaseProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val doesThrow by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)
}
