package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.type.SirDeclaredSirType

object InitializeSirSuperTypesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        initializeSuperTypes()

        // First invocation does not correctly initialize type arguments due to unsatisfied type parameters bounds (because other types do not have the super types yet).
        initializeSuperTypes()
    }

    private fun SirPhase.Context.initializeSuperTypes() {
        oirProvider.allClassesAndProtocols.forEach {
            initializeSuperTypes(it)
        }
    }

    context(SirPhase.Context)
    private fun initializeSuperTypes(oirClass: OirClass) {
        val sirSuperTypes = oirClass.superTypes
            .map { sirTypeTranslator.mapType(it) }
            .map { it.evaluate().type }
            .filterIsInstance<SirDeclaredSirType>()
            .takeIf { it.isNotEmpty() }
            ?: oirClass.defaultSuperTypes

        oirClass.originalSirClass.superTypes.clear()
        oirClass.originalSirClass.superTypes.addAll(sirSuperTypes)
    }

    context(SirPhase.Context)
    private val OirClass.defaultSuperTypes: List<SirDeclaredSirType>
        get() = when (kind) {
            OirClass.Kind.Class -> listOf(sirBuiltins.Swift.AnyObject.defaultType, sirBuiltins.Swift.Hashable.defaultType)
            OirClass.Kind.Protocol -> listOf(sirBuiltins.Swift.AnyObject.defaultType)
        }
}
