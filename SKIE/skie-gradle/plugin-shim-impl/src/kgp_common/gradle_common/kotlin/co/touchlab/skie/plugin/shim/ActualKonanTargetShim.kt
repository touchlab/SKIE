package co.touchlab.skie.plugin.shim

import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName

class ActualKonanTargetShim(
    private val konanTarget: KonanTarget,
) : KonanTargetShim {

    override val name: String = konanTarget.name

    override val presetName: String = konanTarget.presetName

    override fun toString(): String =
        konanTarget.toString()

    override fun equals(other: Any?): Boolean =
        konanTarget == other

    override fun hashCode(): Int =
        konanTarget.hashCode()
}
