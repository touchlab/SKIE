package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationNamespace
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toFqNameTypeFromEnclosingTypeParameters

// Needed due to a bug in Swift compiler that incorrectly resolves bridges nested in other declarations.
object MoveBridgesToTopLevelPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses
            .mapNotNull { it.bridgedSirClass }
            .forEach {
                it.moveToTopLevel()
            }
    }
}

private fun SirClass.moveToTopLevel() {
    val namespace = namespace ?: return

    createReplacementTypeAlias(namespace)

    renameAndRemoveFromNamespace()
}

private fun SirClass.createReplacementTypeAlias(namespace: SirDeclarationNamespace) {
    val typeAlias = SirTypeAlias(
        baseName = baseName,
        parent = namespace,
        typeFactory = { typeAlias ->
            this.toFqNameTypeFromEnclosingTypeParameters(typeAlias.typeParameters)
        },
    )

    typeAlias.copyTypeParametersFrom(this)
    if (publicTypeAlias == null) {
        publicTypeAlias = typeAlias
    }
    if (internalTypeAlias == null) {
        internalTypeAlias = typeAlias
    }
}

private fun SirClass.renameAndRemoveFromNamespace() {
    baseName = "Bridge__${fqName.toLocalString().replace(".", "_")}"
    isHidden = true
    isReplaced = true
    namespace = null
}
