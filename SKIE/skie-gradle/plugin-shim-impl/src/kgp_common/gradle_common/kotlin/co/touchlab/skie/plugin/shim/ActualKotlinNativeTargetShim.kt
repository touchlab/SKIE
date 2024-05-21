package co.touchlab.skie.plugin.shim

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class ActualKotlinNativeTargetShim(
    private val kotlinNativeTarget: KotlinNativeTarget,
    objectFactory: ObjectFactory,
) : KotlinNativeTargetShim, Named by kotlinNativeTarget {

    override val compilations: NamedDomainObjectContainer<KotlinNativeCompilationShim> =
        objectFactory.domainObjectContainer(KotlinNativeCompilationShim::class.java)

    init {
        kotlinNativeTarget.compilations.configureEach {
            val shim = ActualKotlinNativeCompilationShim(this, this@ActualKotlinNativeTargetShim)

            compilations.add(shim)
        }
    }

    override fun toString(): String =
        kotlinNativeTarget.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActualKotlinNativeTargetShim) return false

        if (kotlinNativeTarget != other.kotlinNativeTarget) return false

        return true
    }

    override fun hashCode(): Int =
        kotlinNativeTarget.hashCode()
}
