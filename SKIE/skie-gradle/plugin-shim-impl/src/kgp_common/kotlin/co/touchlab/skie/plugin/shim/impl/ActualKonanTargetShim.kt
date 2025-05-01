package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.KonanTargetShim
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName

class ActualKonanTargetShim(private val konanTarget: KonanTarget) : KonanTargetShim {

    override val name: String = konanTarget.name

    override val presetName: String = konanTarget.presetName

    override fun toString(): String = konanTarget.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActualKonanTargetShim) return false

        if (konanTarget != other.konanTarget) return false

        return true
    }

    override fun hashCode(): Int = konanTarget.hashCode()
}
