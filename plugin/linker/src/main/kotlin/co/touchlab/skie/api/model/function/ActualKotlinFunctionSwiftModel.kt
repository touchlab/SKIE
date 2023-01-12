package co.touchlab.skie.api.model.function

import co.touchlab.skie.api.model.parameter.ActualKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class ActualKotlinFunctionSwiftModel(
    functionDescriptor: FunctionDescriptor,
    override var receiver: MutableKotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinFunctionSwiftModel(functionDescriptor, receiver, namer), MutableKotlinFunctionSwiftModel {

    override var identifier: String = super.identifier

    override val parameters: List<MutableKotlinParameterSwiftModel> =
        super.parameters.map { ActualKotlinParameterSwiftModel(it.argumentLabel) }

    override var visibility: SwiftModelVisibility = super.visibility

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(functionDescriptor, receiver, namer)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility || parameters.any { it.isChanged }
}

