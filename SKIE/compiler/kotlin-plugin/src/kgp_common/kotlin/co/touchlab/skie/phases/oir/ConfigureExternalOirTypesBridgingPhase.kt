package co.touchlab.skie.phases.oir

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.util.Command
import java.io.File

class ConfigureExternalOirTypesBridgingPhase(
    val context: SirPhase.Context,
) : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val apiNotesEntries = getRelevantExternalApiNotesEntries(configurables.absoluteTargetSysRoot)

        apiNotesEntries.forEach {
            configureBridging(it)
        }
    }

    private fun getRelevantExternalApiNotesEntries(sdkPath: String): List<ApiNotesEntry> {
        val apiNotesFiles = getAllApiNotesFiles(sdkPath)

        return apiNotesFiles
            .flatMap { parseApiNotesFile(it) }
    }

    private fun getAllApiNotesFiles(sdkPath: String): List<File> {
        val grepResult = Command(
            "grep",
            "--include=*.apinotes",
            "--recursive",
            "--files-with-matches",
            "-E",
            "SwiftBridge:|SwiftName:",
            sdkPath.trimEnd('/') + '/',
        ).execute()

        return grepResult.outputLines
            .map { File(it) }
            .filter { it.exists() && it.isFile }
    }

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

    context(SirPhase.Context)
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

    context(SirPhase.Context)
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
