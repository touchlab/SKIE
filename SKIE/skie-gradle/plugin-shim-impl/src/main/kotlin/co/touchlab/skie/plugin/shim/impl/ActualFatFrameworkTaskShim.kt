package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.FatFrameworkTaskShim
import co.touchlab.skie.plugin.shim.FrameworkShim
import co.touchlab.skie.plugin.shim.impl.util.darwinTarget
import co.touchlab.skie.util.directory.FrameworkLayout
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

class ActualFatFrameworkTaskShim(
    override val task: FatFrameworkTask,
) : FatFrameworkTaskShim {

    init {
        val distinctOsTypes = task.frameworks.map { it.darwinTarget.targetTriple.isMacos }.distinct()
        check(distinctOsTypes.size <= 1) {
            "Fat frameworks can only be all targeting the macOS or none can. Found: ${task.frameworks.map { it.darwinTarget.targetTriple }.distinct()}"
        }
    }

    override val targetFrameworkLayout: FrameworkLayout = FrameworkLayout(
        frameworkDirectory = task.fatFramework,
        isMacosFramework = task.frameworks.firstOrNull()?.darwinTarget?.targetTriple?.isMacos ?: false,
    )

    override val frameworks: List<FrameworkShim>
        get() = task.frameworks.map { ActualFrameworkShim(it) }
}
