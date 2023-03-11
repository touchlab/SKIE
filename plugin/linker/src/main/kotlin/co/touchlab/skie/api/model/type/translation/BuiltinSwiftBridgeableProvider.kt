package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.api.apinotes.builder.ApiNotes
import co.touchlab.skie.plugin.Command
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.type.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnySirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceSirType
import co.touchlab.skie.plugin.api.sir.declaration.isHashable
import org.jetbrains.kotlin.name.FqName
import java.io.File

class BuiltinSwiftBridgeableProvider(
    private val sdkPath: String,
    private val declarationRegistry: SwiftIrDeclarationRegistry,
) {
    val builtinBridges: Map<FqName, SwiftClassSirType> by lazy {
        getAllBuiltinBridges()
    }

    fun bridgeFor(fqName: FqName, swiftExportScope: SwiftExportScope): SwiftNonNullReferenceSirType? {
        val bridge = builtinBridges[fqName] ?: return null
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> null
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) && bridge.declaration.isHashable() -> SwiftAnyHashableSirType
            else -> bridge
        }
    }

    private fun getAllBuiltinBridges(): Map<FqName, SwiftClassSirType> {
        val apiNotesFiles = getAllApiNotesFiles()
        return apiNotesFiles
            .map { file ->
                val apiNotes = ApiNotes(file)
                val moduleName = apiNotes.moduleName
                // TODO: Protocols can't really have bridges, do we want to keep them here?
                val allTypes = apiNotes.classes + apiNotes.protocols

                allTypes
                    .mapNotNull { apiNotesType ->
                        apiNotesType.bridgeFqName?.let { bridgeFqName ->
                            getKotlinFqName(moduleName, apiNotesType.objCFqName) to getSwiftModelFor(getSwiftFqName(moduleName, bridgeFqName))
                        }
                    }
            }
            .flatten()
            .toMap()
    }

    private fun getAllApiNotesFiles(): List<File> {
        val grepResult = Command(
            "grep",
            "--include=*.apinotes",
            "--recursive", "--files-with-matches", "--word-regexp",
            sdkPath.trimEnd('/') + '/',
            "--regexp=SwiftBridge:",
        ).execute()

        return grepResult.outputLines
            .map {
                File(it)
            }
            .filter {
                it.exists() && it.isFile
            }
    }

    private fun getSwiftModelFor(swiftFqName: SwiftFqName.External): SwiftClassSirType {
        return SwiftClassSirType(
            declaration = declarationRegistry.referenceExternalTypeDeclaration(swiftFqName),
            typeArguments = when (swiftFqName) {
                BuiltinDeclarations.Swift.Array.publicName -> listOf(SwiftAnySirType)
                BuiltinDeclarations.Swift.Dictionary.publicName -> listOf(SwiftAnyHashableSirType, SwiftAnySirType)
                BuiltinDeclarations.Swift.Set.publicName -> listOf(SwiftAnyHashableSirType)
                else -> emptyList()
            }
        )
    }

    private fun getSwiftFqName(module: String, bridgeFqName: String): SwiftFqName.External.TopLevel {
        val bridgeModule = bridgeFqName.substringBefore(".", missingDelimiterValue = "").ifBlank { module }
        val bridgeName = bridgeFqName.substringAfter(".")

        // APINotes bridging doesn't support nested types, so we don't need to either.
        return SwiftFqName.External.TopLevel(
            module = bridgeModule,
            name = bridgeName,
        )
    }

    private fun getKotlinFqName(module: String, type: String): FqName = FqName("platform.${module}.${type}")

    companion object {
        private val swiftArray = "Swift.Array"
        private val swiftDictionary = "Swift.Dictionary"
        private val swiftSet = "Swift.Set"

        private val nonHashableTypes = setOf(
            swiftArray, swiftDictionary
        )
    }
}
