package co.touchlab.skie.api.model.property.extension

import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.KotlinInterfaceExtensionPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.MutableKotlinInterfaceExtensionPropertySwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinInterfaceExtensionPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val getter: MutableKotlinFunctionSwiftModel,
    override val setter: MutableKotlinFunctionSwiftModel?,
) : MutableKotlinInterfaceExtensionPropertySwiftModel {

    override val original: KotlinInterfaceExtensionPropertySwiftModel = OriginalKotlinInterfaceExtensionPropertySwiftModel(
        descriptor,
        getter,
        setter,
    )
}
