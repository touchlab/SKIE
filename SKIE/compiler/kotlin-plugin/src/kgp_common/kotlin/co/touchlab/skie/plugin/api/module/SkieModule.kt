package co.touchlab.skie.plugin.api.module

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.sir.element.SirFile
import io.outfoxx.swiftpoet.FileSpec

interface SkieModule {

    fun configure(ordering: Ordering = Ordering.InOrder, configure: context(MutableSwiftModelScope) () -> Unit)

    fun file(
        name: String,
        namespace: String = SirFile.skieNamespace,
        ordering: Ordering = Ordering.InOrder,
        contents: context(SwiftModelScope) FileSpec.Builder.() -> Unit,
    )

    fun staticFile(name: String, namespace: String = SirFile.skieNamespace, contents: () -> String)

    enum class Ordering {
        InOrder, First, Last
    }
}
