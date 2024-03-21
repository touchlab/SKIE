package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object DeleteSkieFrameworkContentPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        framework.swiftHeader.delete()
        framework.swiftModuleParent.deleteRecursively()
        framework.swiftModuleParent.mkdirs()
    }
}
