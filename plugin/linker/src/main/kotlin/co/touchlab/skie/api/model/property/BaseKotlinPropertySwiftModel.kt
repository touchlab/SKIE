package co.touchlab.skie.api.model.property

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

abstract class BaseKotlinPropertySwiftModel(
    final override val descriptor: PropertyDescriptor,
    override val receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : KotlinPropertySwiftModel {

    override val identifier: String = namer.getPropertyName(descriptor)

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val objCName: String = namer.getPropertyName(descriptor)
}
