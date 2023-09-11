package co.touchlab.skie.phases

import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
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
