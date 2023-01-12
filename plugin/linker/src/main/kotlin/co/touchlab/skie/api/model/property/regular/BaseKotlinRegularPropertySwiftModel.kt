package co.touchlab.skie.api.model.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

abstract class BaseKotlinRegularPropertySwiftModel(
    final override val descriptor: PropertyDescriptor,
    override val receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : KotlinRegularPropertySwiftModel {

    override val identifier: String = namer.getPropertyName(descriptor)

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val objCName: String = namer.getPropertyName(descriptor)
}
