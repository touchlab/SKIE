package co.touchlab.skie.util

class SharedCounter {

    private var next: Int = 0

    fun next(): Int = next++
}
