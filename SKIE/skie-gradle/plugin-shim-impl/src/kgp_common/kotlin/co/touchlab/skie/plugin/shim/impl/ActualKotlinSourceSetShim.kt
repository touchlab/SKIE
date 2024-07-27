package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class ActualKotlinSourceSetShim(
    private val kotlinSourceSet: KotlinSourceSet,
) : KotlinSourceSetShim {

    override val name: String
        get() = kotlinSourceSet.name

    override val dependsOn: Set<KotlinSourceSetShim>
        get() = kotlinSourceSet.dependsOn.map { ActualKotlinSourceSetShim(it) }.toSet()

    override fun toString(): String =
        kotlinSourceSet.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActualKotlinSourceSetShim) return false

        if (kotlinSourceSet != other.kotlinSourceSet) return false

        return true
    }

    override fun hashCode(): Int =
        kotlinSourceSet.hashCode()
}
