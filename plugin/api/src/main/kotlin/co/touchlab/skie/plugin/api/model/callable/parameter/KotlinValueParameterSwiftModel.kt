package co.touchlab.skie.plugin.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

interface KotlinValueParameterSwiftModel {

    val descriptor: ParameterDescriptor?
        get() = origin.descriptor

    val origin: Origin

    val original: KotlinValueParameterSwiftModel

    val isChanged: Boolean

    val argumentLabel: String

    val parameterName: String

    val type: TypeSwiftModel

    sealed interface Origin {

        val descriptor: ParameterDescriptor?
            get() = null

        data class Receiver(override val descriptor: ReceiverParameterDescriptor) : Origin

        data class ValueParameter(override val descriptor: ValueParameterDescriptor) : Origin

        object SuspendCompletion : Origin
    }
}
