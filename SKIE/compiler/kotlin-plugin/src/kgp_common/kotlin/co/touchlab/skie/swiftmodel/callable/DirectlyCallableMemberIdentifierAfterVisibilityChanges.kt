package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel

val KotlinDirectlyCallableMemberSwiftModel.identifierAfterVisibilityChanges: String
    get() = when (visibility) {
        SwiftModelVisibility.Visible, SwiftModelVisibility.Hidden -> identifier
        SwiftModelVisibility.Replaced -> "__$identifier"
        SwiftModelVisibility.Removed -> "__Skie_Removed__$identifier"
    }
