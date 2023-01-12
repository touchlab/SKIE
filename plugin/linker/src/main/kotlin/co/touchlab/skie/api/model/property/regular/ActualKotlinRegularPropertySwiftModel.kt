package co.touchlab.skie.api.model.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinRegularPropertySwiftModel(
    propertyDescriptor: PropertyDescriptor,
    override val receiver: MutableKotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinRegularPropertySwiftModel(propertyDescriptor, receiver, namer), MutableKotlinRegularPropertySwiftModel {

    override var identifier: String = super.identifier

    override var visibility: SwiftModelVisibility = super.visibility

    override val original: KotlinRegularPropertySwiftModel = OriginalKotlinRegularPropertySwiftModel(propertyDescriptor, receiver, namer)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility
}
