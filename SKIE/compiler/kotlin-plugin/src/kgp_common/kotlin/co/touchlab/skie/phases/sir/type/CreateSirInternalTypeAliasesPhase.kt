package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirVisibility

object CreateSirInternalTypeAliasesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allClasses.forEach {
            createKotlinTypeAlias(it)
        }
    }

    context(SirPhase.Context)
    private fun createKotlinTypeAlias(kirClass: KirClass) {
        val namespace = skieNamespaceProvider.getNamespace(kirClass)

        // WIP Only temporary hack so that we do not need to update Acceptance tests expected errors before we remove type aliases completely
        if (namespace.fqName.toLocalString().contains("Stdlib")) {
            return
        }

        kirClass.originalSirClass.internalTypeAlias = SirTypeAlias(
            baseName = "Kotlin",
            parent = namespace,
            visibility = SirVisibility.PublicButReplaced,
        ) {
            kirClass.originalSirClass.defaultType.withFqName()
        }
    }
}
