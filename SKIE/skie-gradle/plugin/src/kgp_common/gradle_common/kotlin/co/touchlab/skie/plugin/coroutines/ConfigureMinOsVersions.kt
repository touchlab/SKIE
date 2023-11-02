package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask

internal fun SkieTarget.registerConfigureMinOsVersionTaskIfNeeded() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    val configureMinOsVersionTask = registerSkieTargetBasedTask<ConfigureMinOsVersionTask>("configureMinOsVersion") {
        this.target.set(this@registerConfigureMinOsVersionTaskIfNeeded)
    }

    task.configure {
        dependsOn(configureMinOsVersionTask)
    }
}
