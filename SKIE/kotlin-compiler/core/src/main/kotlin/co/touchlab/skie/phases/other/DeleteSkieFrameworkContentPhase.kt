package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object DeleteSkieFrameworkContentPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.framework.swiftHeader.delete()
        context.framework.swiftModuleParent.deleteRecursively()
        context.framework.swiftModuleParent.mkdirs()
    }
}
