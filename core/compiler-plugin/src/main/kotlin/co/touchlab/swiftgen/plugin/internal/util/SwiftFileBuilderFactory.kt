package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.FileSpec

internal class SwiftFileBuilderFactory(val swiftPackModuleBuilder: SwiftPackModuleBuilder) {

    private val builders = mutableListOf<FileSpec.Builder>()

    fun create(name: String): FileSpec.Builder =
        FileSpec.builder(name)
            .also { builders.add(it) }

    fun buildAll(): List<FileSpec> =
        builders.map { it.build() }
}