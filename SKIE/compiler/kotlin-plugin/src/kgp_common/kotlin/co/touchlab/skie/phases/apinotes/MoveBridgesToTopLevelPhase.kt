package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationNamespace
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters

// Needed due to a bug in Swift compiler that incorrectly resolves bridges nested in other declarations.
object MoveBridgesToTopLevelPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        exposedTypes.mapNotNull { it.bridgedSirClass }
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
        simpleName = simpleName,
        parent = namespace,
        typeFactory = { typeAlias ->
            this.toTypeFromEnclosingTypeParameters(typeAlias.typeParameters).also { it.useInternalName = false }
        },
    )

    typeAlias.copyTypeParametersFrom(this)
    if (internalTypeAlias == null) {
        internalTypeAlias = typeAlias
    }
}

private fun SirClass.renameAndRemoveFromNamespace() {
    simpleName = "__Skie__Bridge__${fqName.toLocalUnescapedNameString().replace(".", "_")}"
    this.namespace = null
}