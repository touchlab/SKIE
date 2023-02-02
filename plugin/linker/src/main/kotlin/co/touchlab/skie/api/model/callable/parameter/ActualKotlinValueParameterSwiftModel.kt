package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel.Origin
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

internal class ActualKotlinValueParameterSwiftModel(
    core: KotlinParameterSwiftModelCore,
    parameterDescriptor: ParameterDescriptor?,
    getParameterType: () -> TypeSwiftModel,
) : MutableKotlinValueParameterSwiftModel {

    override val origin: Origin = core.getOrigin(parameterDescriptor)

    override var argumentLabel: String by core::argumentLabel

    override val parameterName: String by core::parameterName

    override val original: KotlinValueParameterSwiftModel = OriginalKotlinValueParameterSwiftModel(this)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel

    override val type: TypeSwiftModel by lazy(getParameterType)

    override fun toString(): String = origin.toString()
}
