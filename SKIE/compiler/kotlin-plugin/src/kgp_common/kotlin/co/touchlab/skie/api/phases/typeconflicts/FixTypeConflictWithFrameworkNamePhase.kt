package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.util.Reporter

class FixTypeConflictWithFrameworkNamePhase(
    private val skieModule: SkieModule,
    private val framework: FrameworkLayout,
    private val reporter: Reporter,
) : SkieLinkingPhase {

    private val frameworkName = SwiftFqName.Local.TopLevel(framework.moduleName)

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.Last) {
            fixTypeNameCollisions()
        }
    }

    private fun MutableSwiftModelScope.fixTypeNameCollisions() {
        var collisionExists = false

        exposedTypes.forEach { type ->
            if (type.nonBridgedDeclaration.publicName == frameworkName) {
                type.identifier += "_"
                collisionExists = true
            }

            val localBridgeDeclaration = type.asSkieGeneratedSwiftType()
            if (localBridgeDeclaration?.publicName == frameworkName) {
                localBridgeDeclaration.swiftName += "_"
                collisionExists = true
            }
        }

        if (collisionExists) {
            logModuleNameCollisionWarning()
        }
    }

    private fun MutableKotlinTypeSwiftModel.asSkieGeneratedSwiftType(): SwiftIrTypeDeclaration.Local.SKIEGeneratedSwiftType? =
        (this.bridge as? ObjcSwiftBridge.FromSKIE)?.declaration as? SwiftIrTypeDeclaration.Local.SKIEGeneratedSwiftType

    private fun logModuleNameCollisionWarning() {
        reporter.warning(
            "Type '${framework.moduleName}' was renamed to '${framework.moduleName}_' " +
                    "because it has the same name as the produced framework which is forbidden.",
        )
    }
}
