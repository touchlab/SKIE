package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface KotlinFunctionSwiftModel : KotlinDirectlyCallableMemberSwiftModel {

    override val descriptor: FunctionDescriptor

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModel>

    val role: Role

    val returnType: SirType

    val returnTypeFlowMappingStrategy: FlowMappingStrategy

    val objCReturnType: ObjCType?

    val valueParameters: List<KotlinValueParameterSwiftModel>

    val objCSelector: String

    val isSuspend: Boolean

    val isThrowing: Boolean

    enum class Role {
        SimpleFunction, Constructor, ConvertedGetter, ConvertedSetter
    }
}
