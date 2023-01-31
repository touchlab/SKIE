package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

interface KotlinRegularPropertySwiftModel : KotlinPropertySwiftModel, KotlinDirectlyCallableMemberSwiftModel {

    override val original: KotlinRegularPropertySwiftModel

    val type: TypeSwiftModel

    val objCName: String

    override val reference: String
        get() = when (visibility) {
            SwiftModelVisibility.Visible, SwiftModelVisibility.Hidden -> identifier
            SwiftModelVisibility.Replaced -> "__$identifier"
            SwiftModelVisibility.Removed -> "__Skie_Removed__$identifier"
        }

    /**
     * Properties have their name equal to reference.
     */
    override val name: String
        get() = reference
}
