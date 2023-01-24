package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

data class SwiftTypeSwiftModel(
    override val containingType: TypeSwiftModel?,
    override val identifier: String,
    val isHashable: Boolean,
) : TypeSwiftModel {

    override val stableFqName: String
        get() {
            return TypeSwiftModel.StableFqNameNamespace + "swift__${fqName("_")}"
        }

    override val bridgedOrStableFqName: String
        get() = stableFqName

    override val isSwiftSymbol: Boolean = true
    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.None

    override fun fqName(separator: String): String {
        val parentName = containingType?.fqName(separator) ?: return identifier

        return "$parentName${separator}$identifier"
    }
}
