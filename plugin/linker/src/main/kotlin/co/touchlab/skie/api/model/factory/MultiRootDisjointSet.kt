package co.touchlab.skie.api.model.factory

class MultiRootDisjointSet<T>(private val getParents: (T) -> Collection<T>) {

    val sets: Set<Set<T>>
        get() = setByElement.values.toSet()

    private val setByElement = mutableMapOf<T, MutableSet<T>>()

    fun add(element: T) {
        if (element in setByElement) {
            return
        }

        setByElement[element] = mutableSetOf(element)

        val parents = getParents(element)
        addAll(parents)

        (parents + element).map(setByElement::getValue).reduce(::unify)
    }

    fun addAll(elements: Collection<T>) {
        elements.forEach {
            add(it)
        }
    }

    private fun unify(lhs: MutableSet<T>, rhs: MutableSet<T>): MutableSet<T> {
        if (lhs == rhs) {
            return lhs
        }

        val (source, target) = if (lhs.size < rhs.size) lhs to rhs else rhs to lhs

        target.addAll(source)

        source.forEach {
            setByElement[it] = target
        }

        return target
    }
}
