package co.touchlab.skie.plugin.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType

interface KotlinValueParameterSwiftModel {

    val descriptor: ParameterDescriptor?
        get() = origin.descriptor

    val origin: Origin

    val original: KotlinValueParameterSwiftModel

    val isChanged: Boolean

    val argumentLabel: String

    val parameterName: String

    val type: TypeSwiftModel

    val objCType: ObjCType

    val position: Int

    val flowMappingStrategy: FlowMappingStrategy

    sealed interface Origin {

        val descriptor: ParameterDescriptor?
            get() = null

        data class Receiver(override val descriptor: ReceiverParameterDescriptor) : Origin

        data class ValueParameter(override val descriptor: ValueParameterDescriptor) : Origin

        object SuspendCompletion : Origin
    }
}
