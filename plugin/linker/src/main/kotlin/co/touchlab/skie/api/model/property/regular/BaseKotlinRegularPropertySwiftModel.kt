package co.touchlab.skie.api.model.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

abstract class BaseKotlinRegularPropertySwiftModel(
    final override val descriptor: PropertyDescriptor,
    namer: ObjCExportNamer,
) : KotlinRegularPropertySwiftModel {

    override val identifier: String = namer.getPropertyName(descriptor).swiftName

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val objCName: String = namer.getPropertyName(descriptor).objCName
}
