package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

sealed interface SwiftTypeModel: TypeSwiftModel {
    override val swiftGenericExportScope: SwiftGenericExportScope
        get() = SwiftGenericExportScope.None

    override val containingType: TypeSwiftModel?
        get() = null

    override val identifier: String
        get() = stableFqName

    override val bridgedOrStableFqName: String
        get() = stableFqName

    override val isSwiftSymbol: Boolean
        get() = true

    override fun fqName(separator: String): String {
        return stableFqName
    }
}
