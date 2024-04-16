package co.touchlab.skie.oir.element

import co.touchlab.skie.util.pop

sealed interface OirOverridableDeclaration<T : OirOverridableDeclaration<T>> : OirCallableDeclaration {

    val isFakeOverride: Boolean

    val overriddenDeclarations: List<T>

    val overriddenBy: List<T>

    fun addOverride(declaration: T)

    fun addOverriddenBy(declaration: T)
}

class OirOverridableDeclarationDelegate<T : OirOverridableDeclaration<T>>(
    private val self: T,
) {

    val overriddenDeclarations = mutableListOf<T>()

    val overriddenBy = mutableListOf<T>()

    fun addOverride(declaration: T) {
        if (declaration in overriddenDeclarations) return

        overriddenDeclarations += declaration
        declaration.addOverriddenBy(self)
    }

    fun addOverriddenBy(declaration: T) {
        if (declaration in overriddenBy) return

        overriddenBy += declaration
        declaration.addOverride(self)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : OirOverridableDeclaration<T>> T.getEntireOverrideHierarchy(): List<T> =
    (this as OirCallableDeclaration).getEntireOverrideHierarchy() as List<T>

@Suppress("DuplicatedCode")
fun OirCallableDeclaration.getEntireOverrideHierarchy(): List<OirCallableDeclaration> {
    if (this !is OirOverridableDeclaration<*>) return listOf(this)

    val visited = mutableSetOf<OirOverridableDeclaration<*>>()

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

fun <T : OirOverridableDeclaration<T>> T.applyToEntireOverrideHierarchy(action: T.() -> Unit) {
    getEntireOverrideHierarchy().forEach {
        it.action()
    }
}

fun <T : OirOverridableDeclaration<T>> T.addOverrides(vararg declarations: T) {
    addOverrides(declarations.toList())
}

fun <T : OirOverridableDeclaration<T>> T.addOverrides(declarations: List<T>) {
    declarations.forEach { addOverride(it) }
}

fun <T : OirOverridableDeclaration<T>> T.addOverriddenBy(vararg declarations: T) {
    addOverriddenBy(declarations.toList())
}

fun <T : OirOverridableDeclaration<T>> T.addOverriddenBy(declarations: List<T>) {
    declarations.forEach { addOverriddenBy(it) }
}
