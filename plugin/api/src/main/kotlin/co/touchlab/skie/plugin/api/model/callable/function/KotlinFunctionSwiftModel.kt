package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface KotlinFunctionSwiftModel : KotlinDirectlyCallableMemberSwiftModel {

    override val descriptor: FunctionDescriptor

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModel>

    val role: Role

    override val original: KotlinFunctionSwiftModel

    val returnType: TypeSwiftModel

    val objCReturnType: ObjCType?

    val valueParameters: List<KotlinValueParameterSwiftModel>

    val objCSelector: String

    val isSuspend: Boolean

    val isThrowing: Boolean

    enum class Role {
        SimpleFunction, Constructor, ConvertedGetter, ConvertedSetter
    }
}
