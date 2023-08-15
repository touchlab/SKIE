package co.touchlab.skie.api.phases

import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import co.touchlab.skie.plugin.api.sir.type.SirType
import co.touchlab.skie.plugin.api.sir.type.SwiftClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftGenericTypeUsageSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftLambdaSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftNullableReferenceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftPointerSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftProtocolSirType
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory

class GenerateFakeObjCDependenciesPhase(
    private val swiftModelScope: DefaultSwiftModelScope,
    private val skieBuildDirectory: SkieBuildDirectory,
) : SkieLinkingPhase {

    override fun execute() {
        getExternalTypes()
            .groupBy { it.module }
            .filter { it.key !in listOf("Swift", "Foundation") }
            .forEach { (module, types) ->
                generateFakeFramework(module, types)
            }
    }

    private fun getExternalTypes(): List<ExternalType> {
        val externalTypesFromCallableMembers = swiftModelScope.allExposedMembers
            .flatMap { it.directlyCallableMembers }
            .flatMap { it.accept(ReferencedTypesVisitor) }
            .flatMap { it.getAllReferencedExternalTypes() }

        val externalTypesFromClasses = swiftModelScope.exposedClasses
            .flatMap { it.nonBridgedDeclaration.getAllReferencedExternalTypes() }

        return (externalTypesFromCallableMembers + externalTypesFromClasses).distinct()
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

        skieBuildDirectory.fakeObjCFrameworks.moduleMap(module).writeTextIfDifferent(modulemapContent)
    }

    private fun generateHeader(module: String, types: List<ExternalType>) {
        val foundationImport = "#import <Foundation/NSObject.h>"
        val typeDeclarations = types.sortedBy { it.name }.joinToString("\n") { it.getHeaderEntry() }

        val headerContent = "$foundationImport\n\n$typeDeclarations"

        skieBuildDirectory.fakeObjCFrameworks.header(module).writeTextIfDifferent(headerContent)
    }
}

private object ReferencedTypesVisitor : KotlinDirectlyCallableMemberSwiftModelVisitor<List<SirType>> {

    override fun visit(function: KotlinFunctionSwiftModel): List<SirType> =
        function.valueParameters.map { it.type } + function.returnType

    override fun visit(regularProperty: KotlinRegularPropertySwiftModel): List<SirType> =
        listOf(regularProperty.type)
}

private fun SirType.getAllReferencedExternalTypes(): List<ExternalType> =
    when (this) {
        is SwiftPointerSirType -> pointee.getAllReferencedExternalTypes()
        is SwiftClassSirType -> declaration.getAllReferencedExternalTypes() + typeArguments.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftGenericTypeUsageSirType -> declaration.getAllReferencedExternalTypes()
        is SwiftLambdaSirType -> returnType.getAllReferencedExternalTypes() + parameterTypes.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftProtocolSirType -> declaration.getAllReferencedExternalTypes()
        is SwiftNullableReferenceSirType -> nonNullType.getAllReferencedExternalTypes()
        else -> emptyList()
    }

private fun SwiftIrDeclaration.getAllReferencedExternalTypes(): List<ExternalType> =
    when (this) {
        is SwiftIrTypeDeclaration.External -> {
            typeParameters.flatMap { it.getAllReferencedExternalTypes() } + ExternalType.Class(module.name, name, typeParameters.size)
        }
        is SwiftIrTypeDeclaration.Local -> typeParameters.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftIrProtocolDeclaration.External -> listOf(ExternalType.Protocol(module.name, name))
        is SwiftIrTypeParameterDeclaration -> bounds.flatMap { it.getAllReferencedExternalTypes() }
        else -> emptyList()
    }

private sealed interface ExternalType {

    val module: String

    val name: String

    fun getHeaderEntry(): String

    data class Class(
        override val module: String,
        override val name: String,
        val typeParameterCount: Int,
    ) : ExternalType {

        override fun getHeaderEntry(): String =
            "@interface $name${getTypeParametersDeclaration()} : NSObject @end"

        private fun getTypeParametersDeclaration(): String =
            if (typeParameterCount == 0) {
                ""
            } else {
                "<${(0 until typeParameterCount).joinToString { "T$it" }}>"
            }
    }

    data class Protocol(
        override val module: String,
        override val name: String,
    ) : ExternalType {

        override fun getHeaderEntry(): String =
            "@protocol $name @end"
    }
}
