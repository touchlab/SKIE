package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.MutableSwiftScope
import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.api.SwiftPoetScope
import io.outfoxx.swiftpoet.FileSpec

class DefaultSkieModule() : SkieModule {

    private val configureBlocks = mutableListOf<context(MutableSwiftScope) () -> Unit>()
    private val swiftPoetFileBlocks = mutableMapOf<String, MutableList<context(SwiftPoetScope) FileSpec.Builder.() -> Unit>>()
    private val textFileBlocks = mutableMapOf<String, MutableList<String>>()
    private var configureBlocksConsumed = false

    override fun configure(configure: context(MutableSwiftScope) () -> Unit) {
        require(!configureBlocksConsumed) { "configure() must not be called again after consumed" }
        configureBlocks.add(configure)
    }

    override fun file(name: String, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit) {
        swiftPoetFileBlocks.getOrPut(name) { mutableListOf() }.add(contents)
    }

    override fun file(name: String, contents: String) {
        textFileBlocks.getOrPut(name) { mutableListOf() }.add(contents)
    }

    fun consumeConfigureBlocks(scope: MutableSwiftScope) {
        configureBlocksConsumed = true
        val result = configureBlocks.toList()
        configureBlocks.clear()

        result.forEach {
            it(scope)
        }
    }

    fun produceSwiftPoetFiles(context: SwiftPoetScope): List<FileSpec> {
        val result = mutableMapOf<String, FileSpec.Builder>()

        do {
            val consumedValues = swiftPoetFileBlocks.toMap()
            swiftPoetFileBlocks.clear()
            consumedValues.forEach { (fileName, blocks) ->
                blocks.forEach { block ->
                    with(context) {
                        block(result.getOrPut(fileName) { FileSpec.builder(fileName) })
                    }
                }
            }
        } while (swiftPoetFileBlocks.isNotEmpty())

        return result.values.map { it.build() }
    }

    fun produceTextFiles(): List<TextFile> {
        val textFiles = textFileBlocks.map { TextFile(it.key, it.value.joinToString("\n")) }

        textFileBlocks.clear()

        return textFiles
    }

    data class TextFile(val name: String, val content: String)
}
