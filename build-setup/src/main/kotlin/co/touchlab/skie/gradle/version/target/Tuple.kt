package co.touchlab.skie.gradle.version.target

typealias Tuple<T> = List<T>

typealias TupleSpace<T> = Set<Tuple<T>>

fun <T> tupleSpaceOf(vararg tuples: Tuple<T>): TupleSpace<T> = setOf(*tuples)

fun <T> tupleOf(vararg elements: T): Tuple<T> = listOf(*elements)

operator fun <T> TupleSpace<T>.times(elements: Collection<T>): TupleSpace<T> =
    flatMap { tuple ->
        tuple * elements
    }.toSet()

operator fun <T> Tuple<T>.times(elements: Collection<T>): TupleSpace<T> =
    elements.map { element ->
        this + element
    }.toSet()
