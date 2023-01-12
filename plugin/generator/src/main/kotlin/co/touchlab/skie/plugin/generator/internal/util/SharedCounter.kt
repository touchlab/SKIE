package co.touchlab.skie.plugin.generator.internal.util

class SharedCounter {

    private var next: Int = 0

    fun next(): Int = next++
}
