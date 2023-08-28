package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary

internal fun NativeBinary.registerConfigureMinOsVersionTaskIfNeeded() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    val task = linkTask.registerSkieLinkBasedTask<ConfigureMinOsVersionTask>("configureMinOsVersion") {
        this.binary.set(this@registerConfigureMinOsVersionTaskIfNeeded)
    }

    linkTask.dependsOn(task)
}
