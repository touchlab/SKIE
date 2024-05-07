package co.touchlab.skie.plugin.shim

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class ActualKotlinSourceSetShim(
    private val kotlinSourceSet: KotlinSourceSet,
) : KotlinSourceSetShim {

    override val name: String
        get() = kotlinSourceSet.name

    override fun toString(): String =
        kotlinSourceSet.toString()

    override fun equals(other: Any?): Boolean =
        kotlinSourceSet == other

    override fun hashCode(): Int =
        kotlinSourceSet.hashCode()
}
