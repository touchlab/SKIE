package co.touchlab.skie.phases.oir

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeParameterParent

class ConfigureExternalOirTypesBridgingPhase(
    val context: SirPhase.Context,
) : SirPhase {

    private val oirProvider = context.oirProvider
    private val sirProvider = context.sirProvider
    private val externalApiNotesProvider = context.externalApiNotesProvider

    context(SirPhase.Context)
    override suspend fun execute() {
        configureBridging()

        loadAllPlatformApiNotesIfEnabled()
    }

    private fun configureBridging() {
        oirProvider.externalClassesAndProtocols.forEach(::configureBridging)
    }

    private fun configureBridging(oirClass: OirClass) {
        val apiNotesEntry = externalApiNotesProvider.findApiNotesEntry(oirClass) ?: return

        if (apiNotesEntry.bridgeSwiftName != null) {
            oirClass.bridgedSirClass = getOrCreateSirClass(apiNotesEntry.bridgeSwiftName)
        }

        if (apiNotesEntry.swiftName != null) {
            oirClass.originalSirClass.baseName = apiNotesEntry.swiftName.simpleName
        }

        if (apiNotesEntry.importAsNonGeneric) {
            oirClass.originalSirClass.typeParameters.toList().forEach {
                it.parent = SirTypeParameterParent.None
            }
        }
    }

    private fun getOrCreateSirClass(fqName: SirFqName): SirClass {
        sirProvider.findClassByFqName(fqName)?.let { return it }

        return SirClass(
            baseName = fqName.simpleName,
            parent = when {
                fqName.parent != null -> getOrCreateSirClass(fqName.parent)
                else -> sirProvider.getExternalModule(fqName.module.name).builtInFile
            },
            // TODO All builtin bridges are structs or enums (not classes which is important for type mapping of reference types, however we do not know if this will be true for 3rd party libraries)
            kind = SirClass.Kind.Struct,
            origin = SirClass.Origin.ExternalSwiftFramework,
        )
        // TODO We do not know if the type is hashable which is important for type mapping
    }

    context(SirPhase.Context)
    private fun loadAllPlatformApiNotesIfEnabled() {
        if (SkieConfigurationFlag.Debug_LoadAllPlatformApiNotes.isEnabled) {
            externalApiNotesProvider.getAllApiNotesEntries()
        }
    }
}
