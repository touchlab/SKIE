package co.touchlab.skie.kir.util

import co.touchlab.skie.util.pop

interface BaseOverridableDeclaration<T : BaseOverridableDeclaration<T>> {

    val isFakeOverride: Boolean

    val overriddenDeclarations: List<T>

    val overriddenBy: List<T>

    fun addOverride(declaration: T)

    fun removeOverride(declaration: T)

    fun addOverriddenBy(declaration: T)

    fun removeOverriddenBy(declaration: T)
}

abstract class BaseOverridableDeclarationDelegate<T : BaseOverridableDeclaration<T>>(
    private val self: T,
) {

    val overriddenDeclarations = mutableListOf<T>()

    val overriddenBy = mutableListOf<T>()

    fun addOverride(declaration: T) {
        if (declaration in overriddenDeclarations) return

        overriddenDeclarations += declaration
        declaration.addOverriddenBy(self)
    }

    fun removeOverride(declaration: T) {
        if (declaration !in overriddenDeclarations) return

        overriddenDeclarations -= declaration
        declaration.removeOverriddenBy(self)
    }

    fun addOverriddenBy(declaration: T) {
        if (declaration in overriddenBy) return

        overriddenBy += declaration
        declaration.addOverride(self)
    }

    fun removeOverriddenBy(declaration: T) {
        if (declaration !in overriddenBy) return

        overriddenBy -= declaration
        declaration.removeOverride(self)
    }
}

@Suppress("UNCHECKED_CAST")
@JvmName("getEntireOverrideHierarchyTyped")
fun <T : BaseOverridableDeclaration<T>> T.getEntireOverrideHierarchy(): List<T> =
    (this as BaseOverridableDeclaration<T>).getEntireOverrideHierarchy() as List<T>

fun <T : BaseOverridableDeclaration<T>> BaseOverridableDeclaration<T>.getEntireOverrideHierarchy(): List<BaseOverridableDeclaration<T>> {
    val visited = mutableSetOf<BaseOverridableDeclaration<T>>()

    val open = mutableListOf(this)

    while (open.isNotEmpty()) {
        val next = open.pop()

        if (next in visited) continue

        visited += next

        open += next.overriddenDeclarations
        open += next.overriddenBy
    }

    return visited.toList()
}

fun <T : BaseOverridableDeclaration<T>> T.applyToEntireOverrideHierarchy(action: T.() -> Unit) {
    getEntireOverrideHierarchy().forEach {
        it.action()
    }
}

fun <T : BaseOverridableDeclaration<T>> T.addOverrides(vararg declarations: T) {
    addOverrides(declarations.toList())
}

fun <T : BaseOverridableDeclaration<T>> T.addOverrides(declarations: List<T>) {
    declarations.forEach { addOverride(it) }
}

fun <T : BaseOverridableDeclaration<T>> T.removeOverrides(vararg declarations: T) {
    removeOverrides(declarations.toList())
}

fun <T : BaseOverridableDeclaration<T>> T.removeOverrides(declarations: List<T>) {
    declarations.forEach { removeOverride(it) }
}

fun <T : BaseOverridableDeclaration<T>> T.addOverriddenBy(vararg declarations: T) {
    addOverriddenBy(declarations.toList())
}

fun <T : BaseOverridableDeclaration<T>> T.addOverriddenBy(declarations: List<T>) {
    declarations.forEach { addOverriddenBy(it) }
}

fun <T : BaseOverridableDeclaration<T>> T.removeOverriddenBy(vararg declarations: T) {
    removeOverriddenBy(declarations.toList())
}

fun <T : BaseOverridableDeclaration<T>> T.removeOverriddenBy(declarations: List<T>) {
    declarations.forEach { removeOverriddenBy(it) }
}

val BaseOverridableDeclaration<*>.isBaseDeclaration: Boolean
    get() = overriddenDeclarations.isEmpty()
