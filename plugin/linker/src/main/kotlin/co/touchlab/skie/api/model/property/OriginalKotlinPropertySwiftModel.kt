package co.touchlab.skie.api.model.property

import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class OriginalKotlinPropertySwiftModel(
    propertyDescriptor: PropertyDescriptor,
    receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinPropertySwiftModel(propertyDescriptor, receiver, namer) {

    override val isChanged: Boolean = false

    override val original: KotlinPropertySwiftModel = this
}
