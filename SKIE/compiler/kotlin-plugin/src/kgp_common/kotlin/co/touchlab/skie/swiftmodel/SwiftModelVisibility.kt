package co.touchlab.skie.swiftmodel

enum class SwiftModelVisibility {
    Visible, Hidden, Replaced, Removed
}

val SwiftModelVisibility.isVisible: Boolean
    get() = this == SwiftModelVisibility.Visible

val SwiftModelVisibility.isHidden
    get() = this == SwiftModelVisibility.Hidden

val SwiftModelVisibility.isReplaced: Boolean
    get() = this == SwiftModelVisibility.Replaced

val SwiftModelVisibility.isRemoved: Boolean
    get() = this == SwiftModelVisibility.Removed

