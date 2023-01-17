package co.touchlab.skie.plugin.api.model.parameter

import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

interface KotlinParameterSwiftModel {

    val descriptor: ParameterDescriptor?
        get() = origin.descriptor

    val origin: Origin

    val original: KotlinParameterSwiftModel

    val isChanged: Boolean

    val argumentLabel: String

    val parameterName: String

    sealed interface Origin {

        val descriptor: ParameterDescriptor?
            get() = null

        data class Receiver(override val descriptor: ReceiverParameterDescriptor) : Origin

        data class ValueParameter(override val descriptor: ValueParameterDescriptor) : Origin

        object SuspendCompletion : Origin

        object ErrorOutParameter : Origin
    }
}
