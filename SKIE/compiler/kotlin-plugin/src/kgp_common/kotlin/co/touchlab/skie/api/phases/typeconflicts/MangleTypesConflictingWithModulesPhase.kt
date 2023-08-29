package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.api.phases.util.ExternalTypesProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule

class MangleTypesConflictingWithModulesPhase(
    private val skieModule: SkieModule,
    private val externalTypesProvider: ExternalTypesProvider,
    private val context: Context,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure {
            mangleConflictingTypes()
        }
    }

    private fun MutableSwiftModelScope.mangleConflictingTypes() {
        val existingTypes = exposedTypes.map { it.publicName }

        val conflictingModules = getConflictingModules()
        val conflictingNames = (conflictingModules + existingTypes).toMutableSet()

        exposedTypes
            .filter { it.publicName in conflictingModules }
            .forEach {
                it.mangle(conflictingNames)
            }
    }

    private fun getConflictingModules(): Set<String> {
        val referencedModules = externalTypesProvider.allReferencedExternalTypes.map { it.module }

        return (externalTypesProvider.builtInModules + referencedModules).toSet()
    }

    private fun MutableKotlinTypeSwiftModel.mangle(conflictingNames: MutableSet<String>) {
        registerReverseOperation(this)

        while (this.publicName in conflictingNames) {
            this.identifier += "_"
        }

        conflictingNames.add(this.publicName)
    }

    private fun registerReverseOperation(swiftModel: MutableKotlinTypeSwiftModel) {
        val originalIdentifier = swiftModel.identifier

        context.reverseOperations.add {
            swiftModel.identifier = originalIdentifier
        }
    }

    class Context {

        internal val reverseOperations = mutableListOf<() -> Unit>()
    }
}

private val KotlinTypeSwiftModel.publicName: String
    get() = swiftIrDeclaration.publicName.toString()
