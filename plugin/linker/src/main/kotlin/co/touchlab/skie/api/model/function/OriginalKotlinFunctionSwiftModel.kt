package co.touchlab.skie.api.model.function

import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class OriginalKotlinFunctionSwiftModel(
    functionDescriptor: FunctionDescriptor,
    receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : BaseKotlinFunctionSwiftModel(functionDescriptor, receiver, namer) {

    override val isChanged: Boolean = false

    override val original: KotlinFunctionSwiftModel = this
}
