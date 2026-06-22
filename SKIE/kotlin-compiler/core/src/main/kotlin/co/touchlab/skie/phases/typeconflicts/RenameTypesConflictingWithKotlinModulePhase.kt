package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.resolveCollisionWithWarning

object RenameTypesConflictingWithKotlinModulePhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        val moduleName = context.sirProvider.kotlinModule.name

        context.sirProvider.allLocalTypeDeclarations.forEach { type ->
            type.resolveCollisionWithWarning {
                if (type.simpleName == moduleName) "the framework name '$moduleName'" else null
            }
        }
    }
}
