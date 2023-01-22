package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.api.model.callable.getReceiverSwiftModel
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinRegularPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>,
    private val core: KotlinRegularPropertySwiftModelCore,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinRegularPropertySwiftModel {

    override val receiver: MutableKotlinTypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.getReceiverSwiftModel(namer)
        }
    }

    override var identifier: String by core::identifier

    override var visibility: SwiftModelVisibility by core::visibility

    override val objCName: String by core::objCName

    override val original: KotlinRegularPropertySwiftModel = OriginalKotlinRegularPropertySwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility

    override val type: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.propertyTypeModel(receiver.swiftGenericExportScope)
        }
}
