package co.touchlab.swiftgen.plugin.internal.util

import io.outfoxx.swiftpoet.FileSpec

internal class FileBuilderFactory {

    private val builders = mutableListOf<FileSpec.Builder>()

    fun create(name: String): FileSpec.Builder =
        FileSpec.builder(name)
            .also { builders.add(it) }

    fun buildAll(): List<FileSpec> =
        builders.map { it.build() }
}