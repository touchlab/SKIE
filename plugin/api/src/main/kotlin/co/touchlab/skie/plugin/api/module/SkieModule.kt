package co.touchlab.skie.plugin.api.module

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import io.outfoxx.swiftpoet.FileSpec

interface SkieModule {

    fun configure(ordering: Ordering = Ordering.InOrder, configure: context(MutableSwiftModelScope) () -> Unit)

    fun file(name: String, ordering: Ordering = Ordering.InOrder, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit)

    fun file(name: String, contents: String)

    enum class Ordering {
        InOrder, First, Last
    }
}
