package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.resolveAsSirClass

class SirHierarchyCache {

    private val primaryCache = mutableMapOf<SirClass, MutableMap<SirClass, Boolean>>()

    private val inheritanceCache = mutableMapOf<SirClass, MutableMap<SirClass, Boolean>>()

    fun SirClass.sharesDirectInheritanceHierarchy(other: SirClass): Boolean {
        if (this == other) {
            return true
        }

        if (!this.canTheoreticallyShareDirectInheritanceHierarchy(other)) {
            return false
        }

        primaryCache[this]?.get(other)?.let { return it }

        val result = this.inheritsFrom(other) || other.inheritsFrom(this)

        primaryCache.getOrPut(this) { mutableMapOf() }[other] = result
        primaryCache.getOrPut(other) { mutableMapOf() }[this] = result

        return result
    }

    private fun SirClass.inheritsFrom(other: SirClass): Boolean {
        if (!this.canTheoreticallyInheritFrom(other)) {
            return false
        }

        inheritanceCache[this]?.get(other)?.let { return it }

        val superClasses = superTypes.mapNotNull { it.resolveAsSirClass() }

        val inheritsFrom = other in superClasses || superClasses.any { it.inheritsFrom(other) }

        inheritanceCache.getOrPut(this) { mutableMapOf() }[other] = inheritsFrom
        if (inheritsFrom) {
            inheritanceCache.getOrPut(other) { mutableMapOf() }[this] = false
        }

        return inheritsFrom
    }

    private fun SirClass.canTheoreticallyShareDirectInheritanceHierarchy(other: SirClass): Boolean {
        // TODO Implement based on open/close if added to SirClass
        if ((this.kind.isStruct || this.kind.isEnum) && !other.kind.isProtocol) return false
        if ((other.kind.isStruct || other.kind.isEnum) && !this.kind.isProtocol) return false

        return true
    }

    private fun SirClass.canTheoreticallyInheritFrom(other: SirClass): Boolean {
        // TODO Implement based on open/close if added to SirClass
        if (other.kind.isStruct) return false
        if (other.kind.isEnum) return false
        if (other.kind.isClass && !this.kind.isClass) return false

        return true
    }
}
