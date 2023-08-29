@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySetterSwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.doesThrow
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

class DefaultKotlinRegularPropertySetterSwiftModel(
    descriptor: PropertySetterDescriptor,
    namer: ObjCExportNamer,
) : KotlinRegularPropertySetterSwiftModel {

    override val isThrowing: Boolean = namer.mapper.doesThrow(descriptor)
}
