package co.touchlab.skie.phases.oir

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.util.Command
import java.io.File

class ConfigureExternalOirTypesBridgingPhase(
    val context: SirPhase.Context,
) : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val builtinBridges = getAllBuiltinBridges(configurables.absoluteTargetSysRoot)

        builtinBridges.forEach {
            configureBridge(it)
        }
    }

    private fun getAllBuiltinBridges(sdkPath: String): List<BridgeApiNotesEntry> {
        val apiNotesFiles = getAllApiNotesFiles(sdkPath)

        return apiNotesFiles
            .flatMap { parseApiNotesFile(it) }
    }

    private fun getAllApiNotesFiles(sdkPath: String): List<File> {
        val grepResult = Command(
            "grep",
            "--include=*.apinotes",
            "--recursive", "--files-with-matches", "--word-regexp",
            sdkPath.trimEnd('/') + '/',
            "--regexp=SwiftBridge:",
        ).execute()

        return grepResult.outputLines
            .map { File(it) }
            .filter { it.exists() && it.isFile }
    }

    private fun parseApiNotesFile(file: File): List<BridgeApiNotesEntry> {
        val apiNotes = ApiNotes(file)

        val moduleName = apiNotes.moduleName

        // Protocols cannot have bridges
        return apiNotes.classes.mapNotNull {
            val bridgeFqName = it.bridgeFqName ?: return@mapNotNull null

            // API notes bridging doesn't support nested types.
            BridgeApiNotesEntry(moduleName, it.objCFqName, bridgeFqName)
        }
    }

    context(SirPhase.Context)
    private fun configureBridge(bridgeEntry: BridgeApiNotesEntry) {
        val oirClass = oirProvider.findExistingExternalClass(bridgeEntry.moduleName, bridgeEntry.objCName) ?: return

        if (oirClass.bridgedSirClass != null) {
            // Do not override bridge for builtins
            return
        }

        oirClass.bridgedSirClass = SirClass(
            baseName = bridgeEntry.swiftName,
            parent = sirProvider.getExternalModule(bridgeEntry.moduleName),
            // TODO All builtin bridges are structs or enums (not classes which is important for type mapping of reference types, however we do not know if this will be true for 3rd party libraries)
            kind = SirClass.Kind.Struct,
        )
        // TODO We do not know if the type is hashable which is important for type mapping
    }

    private data class BridgeApiNotesEntry(val moduleName: String, val objCName: String, val swiftName: String)
}
