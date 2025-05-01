package co.touchlab.skie.phases.oir.util

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesType
import co.touchlab.skie.phases.apinotes.parser.ExternalApiNotesParser
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.util.Command
import java.io.File

class ExternalApiNotesProvider(private val sdkPath: String, private val sirProvider: SirProvider) {

    private val apiNotesModuleProvidersByModuleName: Map<String, Lazy<ModuleApiNotesProvider>> =
        getApiNotesFiles()
            .groupBy { it.nameWithoutExtension }
            .mapValues { (_, files) ->
                lazy { ModuleApiNotesProvider(files, sirProvider) }
            }

    fun getAllApiNotesEntries(): List<ApiNotesEntry> =
        apiNotesModuleProvidersByModuleName.values.map { it.value }.flatMap { it.getAllApiNotesEntries() }

    fun findApiNotesEntry(oirClass: OirClass): ApiNotesEntry? {
        val module = sirProvider.findModuleForKnownExternalClass(oirClass) ?: return null

        return apiNotesModuleProvidersByModuleName[module.name]?.value?.findApiNotesEntry(oirClass)
    }

    private fun getApiNotesFiles(): List<File> = Command(
        "find",
        sdkPath.removeSuffix("/") + '/',
        "-type",
        "f",
        "-name",
        "*.apinotes",
    )
        .execute()
        .outputLines
        .map { File(it) }
        .filter { it.exists() && it.isFile }

    private class ModuleApiNotesProvider(files: List<File>, private val sirProvider: SirProvider) {

        private val apiNotesEntriesByObjCName: Map<String, Lazy<ApiNotesEntry>> =
            files.flatMap { parseFile(it) }.toMap()

        private fun parseFile(file: File): List<Pair<String, Lazy<ApiNotesEntry>>> = ExternalApiNotesParser.parse(file).let(::parseApiNotes)

        private fun parseApiNotes(apiNotes: ApiNotes): List<Pair<String, Lazy<ApiNotesEntry>>> {
            val module = sirProvider.getExternalModule(apiNotes.moduleName)

            return (apiNotes.classes + apiNotes.protocols).map {
                it.objCFqName to lazy { parseApiNotesType(module, it) }
            }
        }

        private fun parseApiNotesType(module: SirModule, apiNotesType: ApiNotesType): ApiNotesEntry = ApiNotesEntry(
            module = module,
            objCName = apiNotesType.objCFqName,
            swiftName = apiNotesType.swiftFqName?.let { SirFqName(module, it) },
            bridgeSwiftName = apiNotesType.bridgeFqName?.let { parseBridgeName(it, module) },
            importAsNonGeneric = apiNotesType.importAsNonGeneric,
        )

        private fun parseBridgeName(bridgeFqName: String, defaultModule: SirModule): SirFqName {
            val parts = bridgeFqName.split('.')

            val (module, className) = when (parts.size) {
                1 -> defaultModule to parts[0]
                2 -> sirProvider.getExternalModule(parts[0]) to parts[1]
                else -> error("Invalid ApiNotes fq name: $this. Expected format is \$moduleName.\$className")
            }

            return SirFqName(module, className)
        }

        fun getAllApiNotesEntries(): List<ApiNotesEntry> = apiNotesEntriesByObjCName.values.map { it.value }

        fun findApiNotesEntry(oirClass: OirClass): ApiNotesEntry? = apiNotesEntriesByObjCName[oirClass.name]?.value
    }

    data class ApiNotesEntry(
        val module: SirModule,
        val objCName: String,
        val swiftName: SirFqName?,
        val bridgeSwiftName: SirFqName?,
        val importAsNonGeneric: Boolean,
    )
}
