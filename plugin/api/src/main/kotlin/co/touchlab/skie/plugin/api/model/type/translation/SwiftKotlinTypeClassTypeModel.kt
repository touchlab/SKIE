package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel

data class SwiftKotlinTypeClassTypeModel(
    val model: KotlinTypeSwiftModel,
    val typeArguments: List<SwiftNonNullReferenceTypeModel> = emptyList(),
): SwiftNonNullReferenceTypeModel, KotlinTypeSwiftModel by model {
    override val stableFqName: String
        get() = if (typeArguments.isEmpty()) {
            model.stableFqName
        } else {
            "${model.stableFqName}<${typeArguments.joinToString { it.stableFqName }}>"
        }

    override val bridgedOrStableFqName: String
        get() = model.bridgedOrStableFqName
    override val isSwiftSymbol: Boolean
        get() = model.isSwiftSymbol

    override fun fqName(separator: String): String = model.fqName(separator)
    override val containingType: KotlinClassSwiftModel?
        get() = model.containingType
    override val identifier: String
        get() = model.identifier
    override val swiftGenericExportScope: SwiftGenericExportScope
        get() = model.swiftGenericExportScope
}
