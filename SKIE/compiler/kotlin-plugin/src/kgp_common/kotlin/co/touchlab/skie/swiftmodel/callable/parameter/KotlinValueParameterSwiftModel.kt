package co.touchlab.skie.swiftmodel.callable.parameter

import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

interface KotlinValueParameterSwiftModel {

    val descriptor: ParameterDescriptor?
        get() = origin.descriptor

    val origin: Origin

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
