package co.touchlab.skie.api

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import io.outfoxx.swiftpoet.FileSpec

class DefaultSkieModule : SkieModule {

    private val configureBlocks = OrderedList<context(MutableSwiftModelScope) () -> Unit>()
    private val swiftPoetFileBlocks = mutableMapOf<String, OrderedList<context(SwiftPoetScope) FileSpec.Builder.() -> Unit>>()
    private val textFileBlocks = mutableMapOf<String, MutableList<String>>()
    private var configureBlocksConsumed = false

    override fun configure(ordering: SkieModule.Ordering, configure: context(MutableSwiftModelScope) () -> Unit) {
        require(!configureBlocksConsumed) { "configure() must not be called again after consumed" }
        configureBlocks.add(configure, ordering)
    }

    override fun file(name: String, ordering: SkieModule.Ordering, contents: context(SwiftPoetScope) FileSpec.Builder.() -> Unit) {
        swiftPoetFileBlocks.getOrPut(name) { OrderedList() }.add(contents, ordering)
    }

    override fun file(name: String, contents: String) {
        textFileBlocks.getOrPut(name) { mutableListOf() }.add(contents)
    }

    fun consumeConfigureBlocks(scope: MutableSwiftModelScope) {
        require(!configureBlocksConsumed) { "Configuration already consumed." }
        configureBlocksConsumed = true

        configureBlocks.forEach {
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

    private class OrderedList<T> {

        private val first = mutableListOf<T>()
        private val inOrder = mutableListOf<T>()
        private val last = mutableListOf<T>()

        fun add(element: T, ordering: SkieModule.Ordering) {
            val queue = when (ordering) {
                SkieModule.Ordering.First -> first
                SkieModule.Ordering.InOrder -> inOrder
                SkieModule.Ordering.Last -> last
            }

            queue.add(element)
        }

        fun forEach(action: (T) -> Unit) {
            first.forEach(action)
            inOrder.forEach(action)
            last.forEach(action)
        }
    }
}
