package co.touchlab.swiftpack.api

import java.util.concurrent.atomic.AtomicInteger

class ReferenceMap<T>(
    private val prefix: String,
    private val counter: AtomicInteger,
) {
    private val mutableReferences = mutableMapOf<T, String>()
    private val mutableReverseReferences = mutableMapOf<String, T>()

    val references: Map<T, String> = mutableReferences
    val reverseReferences: Map<String, T> = mutableReverseReferences

    fun getReference(value: T): String {
        return mutableReferences.getOrPut(value) {
            val ref = "$prefix${counter.incrementAndGet()}"
            mutableReverseReferences[ref] = value
            ref
        }
    }

    fun getValue(reference: String): T {
        return mutableReverseReferences[reference] ?: error("No value for reference $reference")
    }
}
