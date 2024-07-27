package co.touchlab.skie.plugin.shim

import co.touchlab.skie.util.TargetTriple
import co.touchlab.skie.util.directory.FrameworkLayout

interface FrameworkShim {

    val name: String

    val targetTriple: TargetTriple

    val layout: FrameworkLayout

    val architectureClangMacro: String

    fun toSerializable(): Serializable = Serializable(
        name = name,
        targetTriple = targetTriple,
        layout = layout,
        architectureClangMacro = architectureClangMacro,
    )

    data class Serializable(
        val name: String,
        val targetTriple: TargetTriple,
        val layout: FrameworkLayout,
        val architectureClangMacro: String,
    )
}

