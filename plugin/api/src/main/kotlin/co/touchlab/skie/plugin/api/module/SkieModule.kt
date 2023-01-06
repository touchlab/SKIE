package co.touchlab.skie.plugin.api.module

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import io.outfoxx.swiftpoet.FileSpec

interface SkieModule {

    fun configure(configure: context(MutableSwiftModelScope) () -> Unit)

    fun file(name: String, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit)

    fun file(name: String, contents: String)
}
