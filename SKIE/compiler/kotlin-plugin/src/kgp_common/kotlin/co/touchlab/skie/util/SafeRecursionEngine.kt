package co.touchlab.skie.util

import org.jetbrains.kotlin.backend.common.pop

class SafeRecursionEngine<INPUT>(private val block: (INPUT) -> Unit) {

    private var isActive = false

    private val stack = mutableListOf<INPUT>()

    fun run(input: INPUT) {
        stack.add(input)

        if (!isActive) {
            processStack(block)
        }
    }

    private fun processStack(block: (INPUT) -> Unit) {
        isActive = true

        while (stack.isNotEmpty()) {
            val current = stack.pop()

            block(current)
        }

        isActive = false
    }
}
