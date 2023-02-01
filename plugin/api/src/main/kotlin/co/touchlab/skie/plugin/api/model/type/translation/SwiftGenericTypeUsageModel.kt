package co.touchlab.skie.plugin.api.model.type.translation

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

data class SwiftGenericTypeUsageModel(
    val typeParameterDescriptor: TypeParameterDescriptor,
    val namer: ObjCExportNamer,
) : SwiftNonNullReferenceTypeModel {

    override val stableFqName: String
        get() = namer.getTypeParameterName(typeParameterDescriptor)
}
