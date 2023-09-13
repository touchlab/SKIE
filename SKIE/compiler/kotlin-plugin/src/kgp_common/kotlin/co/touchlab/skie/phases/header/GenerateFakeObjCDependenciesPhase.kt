package co.touchlab.skie.phases.header

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.util.cache.writeTextIfDifferent

object GenerateFakeObjCDependenciesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allExternalTypesFromNonBuiltinModules
            .groupBy { it.module }
            .forEach { (module, types) ->
                generateFakeFramework(module, types)
            }
    }

    context(SirPhase.Context)
    private fun generateFakeFramework(module: SirModule, types: List<SirTypeDeclaration>) {
        generateModuleMap(module)
        generateHeader(module, types)
    }

    context(SirPhase.Context)
    private fun generateModuleMap(module: SirModule) {
        val modulemapContent =
            """
            framework module ${module.name} {
                umbrella header "${module.name}.h"
            }
        """.trimIndent()

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.moduleMap(module.name).writeTextIfDifferent(modulemapContent)
    }

    context(SirPhase.Context)
    private fun generateHeader(module: SirModule, types: List<SirTypeDeclaration>) {
        val foundationImport = "#import <Foundation/NSObject.h>"
        val typeDeclarations = types
            .sortedBy { it.fqName.toLocalUnescapedNameString() }
            .joinToString("\n") { it.getHeaderEntry() }

        val headerContent = "$foundationImport\n\n$typeDeclarations"

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.header(module.name).writeTextIfDifferent(headerContent)
    }
}

private fun SirTypeDeclaration.getHeaderEntry(): String =
    when (this) {
        is SirClass -> when (kind) {
            SirClass.Kind.Class -> getClassHeaderEntry()
            SirClass.Kind.Enum -> getClassHeaderEntry()
            SirClass.Kind.Struct -> getClassHeaderEntry()
            SirClass.Kind.Protocol -> getProtocolHeaderEntry()
        }
        is SirTypeAlias -> getClassHeaderEntry()
    }

private fun SirTypeDeclaration.getClassHeaderEntry(): String =
    "@interface ${fqName.toLocalUnescapedNameString()}${getTypeParametersDeclaration()} : NSObject @end"

private fun SirTypeDeclaration.getTypeParametersDeclaration(): String =
    if (typeParameters.isEmpty()) {
        ""
    } else {
        typeParameters.joinToString(prefix = "<", postfix = ">") { it.name }
    }

private fun SirClass.getProtocolHeaderEntry(): String =
    "@protocol ${fqName.toLocalUnescapedNameString()} @end"
