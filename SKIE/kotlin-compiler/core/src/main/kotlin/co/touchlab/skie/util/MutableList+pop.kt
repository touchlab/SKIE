package co.touchlab.skie.util

fun <E> MutableList<E>.pop(): E =
    this.removeAt(size - 1)
