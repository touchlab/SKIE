package co.touchlab.skie.api.model.callable

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel

val KotlinDirectlyCallableMemberSwiftModel.identifierAfterVisibilityChanges: String
    get() = when (visibility) {
        SwiftModelVisibility.Visible, SwiftModelVisibility.Hidden -> identifier
        SwiftModelVisibility.Replaced -> "__$identifier"
        SwiftModelVisibility.Removed -> "__Skie_Removed__$identifier"
    }
