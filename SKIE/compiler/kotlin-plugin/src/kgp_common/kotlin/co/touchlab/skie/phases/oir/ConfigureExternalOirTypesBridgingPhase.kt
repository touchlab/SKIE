package co.touchlab.skie.phases.oir

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.parser.ExternalApiNotesParser
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeParameterParent
import co.touchlab.skie.util.Command
import java.io.File

class ConfigureExternalOirTypesBridgingPhase(
    val context: SirPhase.Context,
) : SirPhase {

    private val oirProvider = context.oirProvider
    private val sirProvider = context.sirProvider

    private val sdkPath = context.configurables.absoluteTargetSysRoot.trimEnd('/') + '/'

    context(SirPhase.Context)
    override fun execute() {
        val apiNotesFilesByModuleName = getAllApiNotesByModuleName()

        configureBridging(apiNotesFilesByModuleName)

        loadAllPlatformApiNotesIfEnabled(apiNotesFilesByModuleName)
    }

    private fun getAllApiNotesByModuleName(): Map<String, Lazy<List<ApiNotesEntry>>> =
        Command(
            "find",
            sdkPath,
            "-type",
            "f",
            "-name",
            "*.apinotes",
        )
            .execute()
            .outputLines
            .map { File(it) }
            .filter { it.exists() && it.isFile }
            .groupBy { it.nameWithoutExtension }
            .mapValues { (_, files) ->
                lazy {
                    files.map { ExternalApiNotesParser.parse(it) }.flatMap { getApiNotesEntries(it) }
                }
            }

    private fun getApiNotesEntries(apiNotes: ApiNotes): List<ApiNotesEntry> =
        (apiNotes.classes + apiNotes.protocols)
            .map {
                ApiNotesEntry(
                    moduleName = apiNotes.moduleName,
                    objCName = it.objCFqName,
                    swiftName = it.swiftFqName,
                    bridgeSwiftName = it.bridgeFqName,
                    importAsNonGeneric = it.importAsNonGeneric,
                )
            }
            .filter { it.swiftName != null || it.bridgeSwiftName != null }

    private fun configureBridging(apiNotesFilesByModuleName: Map<String, Lazy<List<ApiNotesEntry>>>) {
        oirProvider.allExternalModules.forEach {
            configureBridging(it, apiNotesFilesByModuleName)
        }
    }

    private fun configureBridging(module: OirModule, apiNotesFilesByModuleName: Map<String, Lazy<List<ApiNotesEntry>>>) {
        val apiNotesEntries = apiNotesFilesByModuleName[module.name]?.value ?: emptyList()

        apiNotesEntries.forEach(::configureBridging)
    }

    private fun configureBridging(apiNotesEntry: ApiNotesEntry) {
        val oirClass = oirProvider.findExistingExternalOirClass(apiNotesEntry.moduleName, apiNotesEntry.objCName) ?: return

        if (oirClass.bridgedSirClass == null && apiNotesEntry.bridgeSwiftName != null) {
            val bridgeFqName = apiNotesEntry.bridgeSwiftName.asApiNotesSirFqName(apiNotesEntry.moduleName)

            oirClass.bridgedSirClass = getOrCreateSirClass(bridgeFqName)
        }

        if (apiNotesEntry.swiftName != null) {
            val module = sirProvider.getExternalModule(apiNotesEntry.moduleName)

            val fqName = SirFqName(module, apiNotesEntry.swiftName)

            if (fqName.parent != null) {
                oirClass.originalSirClass.namespace = getOrCreateSirClass(fqName.parent)
            }

            oirClass.originalSirClass.baseName = fqName.simpleName
        }

        if (apiNotesEntry.importAsNonGeneric) {
            oirClass.originalSirClass.typeParameters.toList().forEach {
                it.parent = SirTypeParameterParent.None
            }
        }
    }

    private fun String.asApiNotesSirFqName(defaultModuleName: String): SirFqName {
        val parts = split('.')

        val (moduleName, className) = when (parts.size) {
            1 -> defaultModuleName to parts[0]
            2 -> parts[0] to parts[1]
            else -> error("Invalid ApiNotes fq name: $this. Expected format is \$moduleName.\$className")
        }

        val module = sirProvider.getExternalModule(moduleName)

        return SirFqName(module, className)
    }

    private fun getOrCreateSirClass(fqName: SirFqName): SirClass {
        sirProvider.findClassByFqName(fqName)?.let { return it }

        return SirClass(
            baseName = fqName.simpleName,
            parent = when {
                fqName.parent != null -> getOrCreateSirClass(fqName.parent)
                else -> sirProvider.getExternalModule(fqName.module.name)
            },
            // TODO All builtin bridges are structs or enums (not classes which is important for type mapping of reference types, however we do not know if this will be true for 3rd party libraries)
            kind = SirClass.Kind.Struct,
        )
        // TODO We do not know if the type is hashable which is important for type mapping
    }

    context(SirPhase.Context)
    private fun loadAllPlatformApiNotesIfEnabled(apiNotesFilesByModuleName: Map<String, Lazy<List<ApiNotesEntry>>>) {
        if (SkieConfigurationFlag.Debug_LoadAllPlatformApiNotes in skieConfiguration.enabledConfigurationFlags) {
            apiNotesFilesByModuleName.forEach {
                it.value
            }
        }
    }

    private data class ApiNotesEntry(
        val moduleName: String,
        val objCName: String,
        val swiftName: String?,
        val bridgeSwiftName: String?,
        val importAsNonGeneric: Boolean,
    )
}
