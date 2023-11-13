package co.touchlab.skie.phases.oir

import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
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
        val apiNotesFilesByModuleName = getAllApiNotesFilesByModuleName()

        oirProvider.allExternalModules.forEach {
            configureBridging(it, apiNotesFilesByModuleName)
        }
    }

    private fun configureBridging(module: OirModule, apiNotesFilesByModuleName: Map<String, List<File>>) {
        val apiNotesEntries = getApiNotesEntries(module, apiNotesFilesByModuleName)

        apiNotesEntries.forEach(::configureBridging)
    }

    private fun getApiNotesEntries(module: OirModule, apiNotesFilesByModuleName: Map<String, List<File>>): List<ApiNotesEntry> =
        apiNotesFilesByModuleName[module.name]?.flatMap { parseApiNotesFile(it) } ?: emptyList()

    private fun getAllApiNotesFilesByModuleName(): Map<String, List<File>> =
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

    private fun parseApiNotesFile(file: File): List<ApiNotesEntry> =
        getApiNotesSegments(file).flatMap { getApiNotesEntries(it) }

    private fun getApiNotesSegments(file: File): List<ApiNotes> {
        val fileLines = file.readLines()

        val moduleNameLine = fileLines.single { it.startsWith("Name:") }

        val otherLines = fileLines - moduleNameLine
        val fileSegments = splitApiNotesFileIntoValidSegments(otherLines)

        return fileSegments.map { segmentLines ->
            val segmentText = (segmentLines + moduleNameLine).joinToString("\n")

            ApiNotes.fromString(segmentText)
        }
    }

    private fun splitApiNotesFileIntoValidSegments(fileLines: List<String>): List<List<String>> {
        val segments = mutableListOf<List<String>>()

        var currentSegment = mutableListOf<String>()

        fileLines
            .filter {
                val trimmedLine = it.trim()

                trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")
            }
            .forEach { line ->
                if (!line.startsWith(" ") && !line.startsWith("-")) {
                    segments.add(currentSegment)

                    currentSegment = mutableListOf()
                }

                currentSegment.add(line)
            }

        segments.add(currentSegment)

        return segments.filter { it.isNotEmpty() }
    }

    private fun getApiNotesEntries(apiNotes: ApiNotes): List<ApiNotesEntry> =
        (apiNotes.classes + apiNotes.protocols)
            .map {
                ApiNotesEntry(
                    moduleName = apiNotes.moduleName,
                    objCName = it.objCFqName,
                    swiftName = it.swiftFqName,
                    bridgeSwiftName = it.bridgeFqName,
                )
            }
            .filter { it.swiftName != null || it.bridgeSwiftName != null }

    private fun configureBridging(apiNotesEntry: ApiNotesEntry) {
        val oirClass = oirProvider.findExistingExternalOirClass(apiNotesEntry.moduleName, apiNotesEntry.objCName) ?: return

        val module = sirProvider.getExternalModule(apiNotesEntry.moduleName)

        if (oirClass.bridgedSirClass == null && apiNotesEntry.bridgeSwiftName != null) {
            val bridgeFqName = SirFqName(module, apiNotesEntry.bridgeSwiftName)

            oirClass.bridgedSirClass = getOrCreateSirClass(bridgeFqName)
        }

        if (apiNotesEntry.swiftName != null) {
            val fqName = SirFqName(module, apiNotesEntry.swiftName)

            if (fqName.parent != null) {
                oirClass.originalSirClass.namespace = getOrCreateSirClass(fqName.parent)
            }

            oirClass.originalSirClass.baseName = fqName.simpleName
        }
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

    private data class ApiNotesEntry(val moduleName: String, val objCName: String, val swiftName: String?, val bridgeSwiftName: String?)
}
