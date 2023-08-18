package co.touchlab.skie.api.phases

import co.touchlab.skie.api.phases.util.ExternalType
import co.touchlab.skie.api.phases.util.ExternalTypesProvider
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory

class GenerateFakeObjCDependenciesPhase(
    private val externalTypesProvider: ExternalTypesProvider,
    private val skieBuildDirectory: SkieBuildDirectory,
) : SkieLinkingPhase {

    override fun execute() {
        externalTypesProvider.allReferencedExternalTypesWithoutBuiltInModules
            .groupBy { it.module }
            .forEach { (module, types) ->
                generateFakeFramework(module, types)
            }
    }

    private fun generateFakeFramework(module: String, types: List<ExternalType>) {
        generateModuleMap(module)
        generateHeader(module, types)
    }

    private fun generateModuleMap(module: String) {
        val modulemapContent =
            """
            framework module $module {
                umbrella header "$module.h"
            }
        """.trimIndent()

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.moduleMap(module).writeTextIfDifferent(modulemapContent)
    }

    private fun generateHeader(module: String, types: List<ExternalType>) {
        val foundationImport = "#import <Foundation/NSObject.h>"
        val typeDeclarations = types.sortedBy { it.name }.joinToString("\n") { it.getHeaderEntry() }

        val headerContent = "$foundationImport\n\n$typeDeclarations"

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.header(module).writeTextIfDifferent(headerContent)
    }
}

private fun ExternalType.getHeaderEntry(): String =
    when (this) {
        is ExternalType.Class -> getHeaderEntry()
        is ExternalType.Protocol -> getHeaderEntry()
    }

private fun ExternalType.Class.getHeaderEntry(): String =
    "@interface $name${getTypeParametersDeclaration()} : NSObject @end"

private fun ExternalType.Class.getTypeParametersDeclaration(): String =
    if (typeParameterCount == 0) {
        ""
    } else {
        "<${(0 until typeParameterCount).joinToString { "T$it" }}>"
    }

private fun ExternalType.Protocol.getHeaderEntry(): String =
    "@protocol $name @end"
