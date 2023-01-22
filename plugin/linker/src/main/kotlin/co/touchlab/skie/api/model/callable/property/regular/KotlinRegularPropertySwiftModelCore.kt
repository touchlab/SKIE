package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class KotlinRegularPropertySwiftModelCore(
    val descriptor: PropertyDescriptor,
    namer: ObjCExportNamer,
) {

    var identifier: String = namer.getPropertyName(descriptor.original)

    var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    val objCName: String = namer.getPropertyName(descriptor.original)
}
