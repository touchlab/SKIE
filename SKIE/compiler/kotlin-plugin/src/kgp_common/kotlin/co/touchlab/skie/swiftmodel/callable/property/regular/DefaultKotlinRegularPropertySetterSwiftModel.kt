@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.compilerinject.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.doesThrow
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

class DefaultKotlinRegularPropertySetterSwiftModel(
    descriptor: PropertySetterDescriptor,
    namer: ObjCExportNamer,
) : KotlinRegularPropertySetterSwiftModel {

    override val isThrowing: Boolean = namer.mapper.doesThrow(descriptor)
}
