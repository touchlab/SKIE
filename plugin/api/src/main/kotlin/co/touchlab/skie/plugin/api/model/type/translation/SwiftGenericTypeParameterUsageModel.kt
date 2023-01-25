package co.touchlab.skie.plugin.api.model.type.translation

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

data class SwiftGenericTypeParameterUsageModel(
    val typeParameterDescriptor: TypeParameterDescriptor,
    val namer: ObjCExportNamer,
): SwiftGenericTypeUsageModel {
    override val typeName: String
        get() = namer.getTypeParameterName(typeParameterDescriptor)

    override val stableFqName: String
        get() = typeName
}
