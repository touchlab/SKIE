package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface KotlinFunctionSwiftModel : KotlinDirectlyCallableMemberSwiftModel {

    override val descriptor: FunctionDescriptor

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModel>

    val role: Role

    override val original: KotlinFunctionSwiftModel

    val returnType: TypeSwiftModel

    val parameters: List<KotlinParameterSwiftModel>

    val objCSelector: String

    override val reference: String
        get() = if (parameters.isEmpty()) {
            identifierAfterVisibilityChanges
        } else {
            "$identifierAfterVisibilityChanges(${parameters.joinToString("") { "${it.argumentLabel}:" }})"
        }

    override val name: String
        get() = if (parameters.isEmpty()) "$identifierAfterVisibilityChanges()" else reference

    enum class Role {
        SimpleFunction, Constructor, ConvertedGetter, ConvertedSetter
    }
}

private val KotlinFunctionSwiftModel.identifierAfterVisibilityChanges: String
    get() = when (visibility) {
        SwiftModelVisibility.Visible, SwiftModelVisibility.Hidden -> identifier
        SwiftModelVisibility.Replaced -> "__$identifier"
        SwiftModelVisibility.Removed -> "__Skie_Removed__$identifier"
    }
