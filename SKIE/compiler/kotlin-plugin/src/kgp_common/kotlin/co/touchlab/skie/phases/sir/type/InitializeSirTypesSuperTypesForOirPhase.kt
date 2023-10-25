package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirDeclaredSirType

object InitializeSirTypesSuperTypesForOirPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.allClassesAndProtocols.forEach {
            initializeSuperTypes(it)
        }
    }

    context(SirPhase.Context)
    private fun initializeSuperTypes(oirClass: OirClass) {
        val sirSuperTypes = oirClass.superTypes
            .map { sirTypeTranslator.mapType(it) }
            .filterIsInstance<DeclaredSirType>()
            .map { it.evaluate().type }
            .takeIf { it.isNotEmpty() }
            ?: oirClass.defaultSuperTypes

        oirClass.originalSirClass.superTypes.addAll(sirSuperTypes)
    }

    context(SirPhase.Context)
    private val OirClass.defaultSuperTypes: List<SirDeclaredSirType>
        get() = when (kind) {
            OirClass.Kind.Class -> listOf(sirBuiltins.Swift.AnyObject.defaultType, sirBuiltins.Swift.Hashable.defaultType)
            OirClass.Kind.Protocol -> listOf(sirBuiltins.Swift.AnyObject.defaultType)
        }
}
