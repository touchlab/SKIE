package co.touchlab.skie.plugin.api.model

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

fun String.applyVisibility(visibility: SwiftModelVisibility): String {
    return when (visibility) {
        SwiftModelVisibility.Visible, SwiftModelVisibility.Hidden, SwiftModelVisibility.Removed -> this
        SwiftModelVisibility.Replaced -> "__$this"
    }
}
