package co.touchlab.swiftpack.api

import io.outfoxx.swiftpoet.FileSpec

interface SkieModule {
    fun configure(configure: context(MutableSwiftScope) () -> Unit)

    fun file(name: String, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit)
}
