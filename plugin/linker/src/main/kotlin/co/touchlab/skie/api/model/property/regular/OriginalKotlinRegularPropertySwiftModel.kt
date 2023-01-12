package co.touchlab.skie.api.model.property.regular

import co.touchlab.skie.plugin.api.model.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class OriginalKotlinRegularPropertySwiftModel(
    propertyDescriptor: PropertyDescriptor,
    override val receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinRegularPropertySwiftModel(propertyDescriptor, namer) {

    override val isChanged: Boolean = false

    override val original: KotlinRegularPropertySwiftModel = this
}
