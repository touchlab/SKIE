package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertyGetterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySetterSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class KotlinRegularPropertySwiftModelCore(
    val descriptor: PropertyDescriptor,
    namer: ObjCExportNamer,
) {

    var identifier: String = namer.getPropertyName(descriptor.original).swiftName

    var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    val getter: KotlinRegularPropertyGetterSwiftModel = DefaultKotlinRegularPropertyGetterSwiftModel(
        descriptor.getter ?: error("$descriptor does not have a getter."),
        namer,
    )

    val setter: KotlinRegularPropertySetterSwiftModel? = descriptor.setter?.let { DefaultKotlinRegularPropertySetterSwiftModel(it, namer) }

    val objCName: String = namer.getPropertyName(descriptor.original).objCName
}
