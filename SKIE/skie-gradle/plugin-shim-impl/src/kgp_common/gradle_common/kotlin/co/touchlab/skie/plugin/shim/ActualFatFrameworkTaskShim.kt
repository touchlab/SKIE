package co.touchlab.skie.plugin.shim

import co.touchlab.skie.util.directory.FrameworkLayout
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

class ActualFatFrameworkTaskShim(
    override val task: FatFrameworkTask,
) : FatFrameworkTaskShim {

    override var baseName: String by task::baseName

    override val targetFrameworkLayout: FrameworkLayout = FrameworkLayout(task.fatFramework)

    override val frameworks: List<FrameworkShim>
        get() = task.frameworks.map { ActualFrameworkShim(it) }
}
