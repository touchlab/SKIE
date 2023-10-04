package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface KotlinFunctionSwiftModel : KotlinDirectlyCallableMemberSwiftModel {

    override val descriptor: FunctionDescriptor

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModel>

    val primarySirFunction: SirFunction
        get() = bridgedSirFunction ?: kotlinSirFunction

    val kotlinSirFunction: SirFunction

    val bridgedSirFunction: SirFunction?

    val primarySirConstructor: SirConstructor
        get() = bridgedSirConstructor ?: kotlinSirConstructor

    val kotlinSirConstructor: SirConstructor

    var bridgedSirConstructor: SirConstructor?

    val kotlinSirValueParameters: List<SirValueParameter>
        get() = when (kotlinSirCallableDeclaration) {
            is SirConstructor -> kotlinSirConstructor.valueParameters
            is SirFunction -> kotlinSirFunction.valueParameters
            else -> error("Unexpected declaration kind $kotlinSirCallableDeclaration.")
        }

    val bridgedSirValueParameters: List<SirValueParameter>?
        get() = when (kotlinSirCallableDeclaration) {
            is SirConstructor -> bridgedSirConstructor?.valueParameters
            is SirFunction -> bridgedSirFunction?.valueParameters
            else -> error("Unexpected declaration kind $kotlinSirCallableDeclaration.")
        }

    val primarySirValueParameters: List<SirValueParameter>
        get() = bridgedSirValueParameters ?: kotlinSirValueParameters

    val role: Role

    val objCReturnType: ObjCType?

    val valueParameters: List<KotlinValueParameterSwiftModel>

    val asyncSwiftModelOrNull: KotlinFunctionSwiftModel?

    val objCSelector: String

    enum class Role {
        SimpleFunction, Constructor, ConvertedGetter, ConvertedSetter
    }
}
