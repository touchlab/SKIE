package co.touchlab.swiftpack.api.impl

import co.touchlab.swiftpack.api.MutableSwiftScope
import co.touchlab.swiftpack.api.SkieModule
import co.touchlab.swiftpack.api.SwiftPoetScope
import io.outfoxx.swiftpoet.FileSpec

class DefaultSkieModule(): SkieModule {
    private val configureBlocks = mutableListOf<context(MutableSwiftScope) () -> Unit>()
    private val fileBlocks = mutableMapOf<String, MutableList<context(SwiftPoetScope) FileSpec.Builder.() -> Unit>>()
    private var configureBlocksConsumed = false

    override fun configure(configure: context(MutableSwiftScope) () -> Unit) {
        require(!configureBlocksConsumed) { "configure() must not be called again after consumed" }
        configureBlocks.add(configure)
    }

    override fun file(name: String, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit) {
        fileBlocks.getOrPut(name) { mutableListOf() }.add(contents)
    }

    fun consumeConfigureBlocks(): List<context(MutableSwiftScope) () -> Unit> {
        configureBlocksConsumed = true
        val result = configureBlocks.toList()
        configureBlocks.clear()
        return result
    }

    fun produceFiles(context: SwiftPoetScope): List<FileSpec> {
        val result = mutableMapOf<String, FileSpec.Builder>()

        do {
            val consumedValues = fileBlocks.toMap()
            fileBlocks.clear()
            consumedValues.forEach { (fileName, blocks) ->
                blocks.forEach { block ->
                    with(context) {
                        block(result.getOrPut(fileName) { FileSpec.builder(fileName) })
                    }
                }
            }
        } while (fileBlocks.isNotEmpty())

        return result.values.map { it.build() }
    }
}
