package co.touchlab.skie.sir.signature

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.resolveAsSirClass

class SirHierarchyCache {

    private val allSuperTypesAndSelfCache = mutableMapOf<SirClass, Set<SirClass>>()

    fun SirClass.sharesDirectInheritanceHierarchy(other: SirClass): Boolean =
        this.isSelfOrInheritsFrom(other) || other.isSelfOrInheritsFrom(this)

    private fun SirClass.isSelfOrInheritsFrom(other: SirClass): Boolean =
        other in this.getAllSuperTypesAndSelf()

    private fun SirClass.getAllSuperTypesAndSelf(): Set<SirClass> =
        allSuperTypesAndSelfCache.getOrPut(this) {
            val superTypes = superTypes.mapNotNull { it.resolveAsSirClass() }.toSet()

            superTypes + superTypes.flatMap { it.getAllSuperTypesAndSelf() } + this
        }
}
