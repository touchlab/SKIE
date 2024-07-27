package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.FrameworkShim
import co.touchlab.skie.plugin.shim.impl.util.darwinTarget
import co.touchlab.skie.util.TargetTriple
import co.touchlab.skie.util.directory.FrameworkLayout
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.konan.target.Architecture

class ActualFrameworkShim(
    framework: FrameworkDescriptor,
) : FrameworkShim {

    override val name: String = framework.name

    override val targetTriple: TargetTriple = framework.darwinTarget.targetTriple

    override val layout: FrameworkLayout = FrameworkLayout(framework.file)

    override val architectureClangMacro: String =
        when (val architecture = framework.target.architecture) {
            Architecture.X86 -> "__i386__"
            Architecture.X64 -> "__x86_64__"
            Architecture.ARM32 -> "__arm__"
            Architecture.ARM64 -> "__aarch64__"
            else -> error("Fat frameworks are not supported for architecture `${architecture.name}`")
        }
}
