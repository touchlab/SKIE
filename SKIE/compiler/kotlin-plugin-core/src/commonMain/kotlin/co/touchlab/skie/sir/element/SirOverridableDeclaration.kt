package co.touchlab.skie.sir.element

import co.touchlab.skie.util.pop

sealed interface SirOverridableDeclaration<T : SirOverridableDeclaration<T>> : SirCallableDeclaration {

    val isFakeOverride: Boolean

    val memberOwner: SirClass?

    val overriddenDeclarations: List<T>

    val overriddenBy: List<T>

    fun addOverride(declaration: T)

    fun removeOverride(declaration: T)

    fun addOverriddenBy(declaration: T)

    fun removeOverriddenBy(declaration: T)
}

class SirOverridableDeclarationDelegate<T : SirOverridableDeclaration<T>>(
    private val self: T,
) {

    val memberOwner: SirClass?
        get() = when (val parent = self.parent) {
            is SirClass -> parent
            is SirExtension -> parent.classDeclaration
            else -> null
        }

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
fun <T : SirOverridableDeclaration<T>> T.getEntireOverrideHierarchy(): List<T> =
    (this as SirCallableDeclaration).getEntireOverrideHierarchy() as List<T>

fun SirCallableDeclaration.getEntireOverrideHierarchy(): List<SirCallableDeclaration> {
    if (this !is SirOverridableDeclaration<*>) return listOf(this)

    val visited = mutableSetOf<SirOverridableDeclaration<*>>()

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

fun <T : SirOverridableDeclaration<T>> T.applyToEntireOverrideHierarchy(action: T.() -> Unit) {
    getEntireOverrideHierarchy().forEach {
        it.action()
    }
}

fun <T : SirOverridableDeclaration<T>> T.addOverrides(vararg declarations: T) {
    addOverrides(declarations.toList())
}

fun <T : SirOverridableDeclaration<T>> T.addOverrides(declarations: List<T>) {
    declarations.forEach { addOverride(it) }
}

fun <T : SirOverridableDeclaration<T>> T.removeOverrides(vararg declarations: T) {
    removeOverrides(declarations.toList())
}

fun <T : SirOverridableDeclaration<T>> T.removeOverrides(declarations: List<T>) {
    declarations.forEach { removeOverride(it) }
}

fun <T : SirOverridableDeclaration<T>> T.addOverriddenBy(vararg declarations: T) {
    addOverriddenBy(declarations.toList())
}

fun <T : SirOverridableDeclaration<T>> T.addOverriddenBy(declarations: List<T>) {
    declarations.forEach { addOverriddenBy(it) }
}

fun <T : SirOverridableDeclaration<T>> T.removeOverriddenBy(vararg declarations: T) {
    removeOverriddenBy(declarations.toList())
}

fun <T : SirOverridableDeclaration<T>> T.removeOverriddenBy(declarations: List<T>) {
    declarations.forEach { removeOverriddenBy(it) }
}
