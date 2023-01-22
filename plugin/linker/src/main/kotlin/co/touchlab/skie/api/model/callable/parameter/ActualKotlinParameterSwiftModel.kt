package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel.Origin
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

internal class ActualKotlinParameterSwiftModel(
    core: KotlinParameterSwiftModelCore,
    parameterDescriptor: ParameterDescriptor?,
    private val getParameterType: () -> TypeSwiftModel,
) : MutableKotlinParameterSwiftModel {

    override val origin: Origin = core.getOrigin(parameterDescriptor)

    override var argumentLabel: String by core::argumentLabel

    override val parameterName: String by core::parameterName

    override val original: KotlinParameterSwiftModel = OriginalKotlinParameterSwiftModel(this)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel

    override val type: TypeSwiftModel
        get() = getParameterType()
}
