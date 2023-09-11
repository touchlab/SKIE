package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.api.apinotes.builder.ApiNotes
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.sir.SirFqName
import co.touchlab.skie.plugin.api.sir.SirProvider
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.type.DeclaredSirType
import co.touchlab.skie.plugin.api.sir.type.NonNullSirType
import co.touchlab.skie.plugin.api.sir.type.SpecialSirType
import co.touchlab.skie.util.Command
import org.jetbrains.kotlin.name.FqName
import java.io.File

class BuiltinSwiftBridgeableProvider(
    private val sdkPath: String,
    private val sirProvider: SirProvider,
) {

    private val sirBuiltins = sirProvider.sirBuiltins

    private val builtinBridges: Map<FqName, DeclaredSirType> by lazy {
        getAllBuiltinBridges()
    }

    fun bridgeFor(fqName: FqName, swiftExportScope: SwiftExportScope): NonNullSirType? {
        val bridge = builtinBridges[fqName] ?: return null
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> null
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) && !bridge.isHashable -> sirBuiltins.Swift.AnyHashable.defaultType
            else -> bridge
        }
    }

    private fun getAllBuiltinBridges(): Map<FqName, DeclaredSirType> {
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
                            getKotlinFqName(moduleName, apiNotesType.objCFqName) to getSwiftModelFor(
                                getSwiftFqName(
                                    moduleName,
                                    bridgeFqName,
                                ),
                            )
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

    private fun getSwiftModelFor(sirFqName: SirFqName): DeclaredSirType {
        return DeclaredSirType(
            // Protocols cannot have bridge
            declaration = sirProvider.getExternalTypeDeclaration(sirFqName, SirClass.Kind.Class),
            typeArguments = when (sirFqName) {
                sirBuiltins.Swift.Array.fqName -> listOf(SpecialSirType.Any)
                sirBuiltins.Swift.Dictionary.fqName -> listOf(sirBuiltins.Swift.AnyHashable.defaultType, SpecialSirType.Any)
                sirBuiltins.Swift.Set.fqName -> listOf(sirBuiltins.Swift.AnyHashable.defaultType)
                else -> emptyList()
            },
        )
    }

    private fun getSwiftFqName(module: String, bridgeFqName: String): SirFqName {
        val bridgeModuleName = bridgeFqName.substringBefore(".", missingDelimiterValue = "").ifBlank { module }
        val bridgeName = bridgeFqName.substringAfter(".")

        val bridgeModule = sirProvider.getExternalModule(bridgeModuleName)

        // APINotes bridging doesn't support nested types, so we don't need to either.
        return SirFqName(
            module = bridgeModule,
            simpleName = bridgeName,
        )
    }

    private fun getKotlinFqName(module: String, type: String): FqName = FqName("platform.${module}.${type}")
}
