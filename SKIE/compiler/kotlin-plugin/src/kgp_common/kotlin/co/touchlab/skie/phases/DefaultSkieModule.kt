package co.touchlab.skie.phases

import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import io.outfoxx.swiftpoet.FileSpec

class DefaultSkieModule : SkieModule {

    private val configureBlocks = OrderedList<context(MutableSwiftModelScope) () -> Unit>()
    // WIP Refactor
    private val swiftPoetFileBlocks = mutableMapOf<Pair<String, String>, OrderedList<context(SwiftModelScope) FileSpec.Builder.() -> Unit>>()
    private val textFileBlocks = mutableMapOf<Pair<String, String>, MutableList<String>>()

    override fun configure(ordering: SkieModule.Ordering, configure: context(MutableSwiftModelScope) () -> Unit) {
        configureBlocks.add(configure, ordering)
    }

    override fun file(name: String, namespace: String, ordering: SkieModule.Ordering, contents: context(SwiftModelScope) FileSpec.Builder.() -> Unit) {
        swiftPoetFileBlocks.getOrPut(name to namespace) { OrderedList() }.add(contents, ordering)
    }

    override fun staticFile(name: String, namespace: String, contents: () -> String) {
        textFileBlocks.getOrPut(name to namespace) { mutableListOf() }.add(contents())
    }

    fun consumeConfigureBlocks(scope: MutableSwiftModelScope) {
        configureBlocks.forEach {
            it(scope)
        }

        configureBlocks.clear()
    }

    context(SwiftModelScope)
    fun produceSwiftPoetFiles(context: SwiftModelScope) {
        val result = mutableMapOf<Pair<String, String>, FileSpec.Builder>()

        val moduleName = sirBuiltins.Kotlin.module.name

        do {
            val consumedValues = swiftPoetFileBlocks.toMap()
            swiftPoetFileBlocks.clear()
            consumedValues.forEach { (fileName, blocks) ->
                blocks.forEach { block ->
                    with(context) {
                        block(result.getOrPut(fileName) { FileSpec.builder(moduleName, fileName.first) })
                    }
                }
            }
        } while (swiftPoetFileBlocks.isNotEmpty())

        result.map { it.key to it.value.build() }.forEach {
            writeToSirFile(it.first.first, it.first.second, it.second.toString())
        }

        return
    }

    context(SwiftModelScope)
    fun produceTextFiles() {
        textFileBlocks.forEach {
            writeToSirFile(it.key.first, it.key.second, it.value.joinToString("\n"))
        }

        textFileBlocks.clear()
    }

    context(SwiftModelScope)
    private fun writeToSirFile(name: String, namespace: String, text: String) {
        val file = sirProvider.getFile(namespace, name)

        val sourceCodeOrNull = file.sourceCode.takeIf { it.isNotBlank() }

        file.sourceCode = listOfNotNull(sourceCodeOrNull, text).joinToString("\n\n")
    }

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

        fun clear() {
            first.clear()
            inOrder.clear()
            last.clear()
        }
    }
}
