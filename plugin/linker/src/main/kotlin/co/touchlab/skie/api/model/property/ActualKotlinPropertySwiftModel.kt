package co.touchlab.skie.api.model.property

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinPropertySwiftModel(
    propertyDescriptor: PropertyDescriptor,
    override val receiver: MutableKotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinPropertySwiftModel(propertyDescriptor, receiver, namer), MutableKotlinPropertySwiftModel {

    override var identifier: String = super.identifier

    override var visibility: SwiftModelVisibility = super.visibility

    override val original: KotlinPropertySwiftModel = OriginalKotlinPropertySwiftModel(propertyDescriptor, receiver, namer)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility
}
