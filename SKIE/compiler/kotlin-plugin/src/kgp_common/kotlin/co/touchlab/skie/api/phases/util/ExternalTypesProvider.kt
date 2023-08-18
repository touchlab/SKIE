package co.touchlab.skie.api.phases.util

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrModule
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import co.touchlab.skie.plugin.api.sir.type.ObjcProtocolSirType
import co.touchlab.skie.plugin.api.sir.type.SirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnyHashableSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnyObjectSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnySirType
import co.touchlab.skie.plugin.api.sir.type.SwiftClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftErrorSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftGenericTypeUsageSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftInstanceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftLambdaSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftMetaClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftNullableReferenceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftPointerSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftPrimitiveSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftProtocolSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftVoidSirType

class ExternalTypesProvider(
    private val swiftModelScope: SwiftModelScope,
) {

    val builtInModules = listOf("Swift", "Foundation")

    val allReferencedExternalTypes: List<ExternalType> = run {
        val externalTypesFromCallableMembers = swiftModelScope.allExposedMembers
            .flatMap { it.directlyCallableMembers }
            .flatMap { it.allBoundedSwiftModels }
            .distinct()
            .flatMap { it.accept(ReferencedTypesVisitor) }
            .flatMap { it.getAllReferencedExternalTypes() }

        val externalTypesFromClasses = swiftModelScope.exposedClasses
            .flatMap { swiftModel ->
                listOf(swiftModel.swiftIrDeclaration, swiftModel.nonBridgedDeclaration)
                    .distinct()
                    .flatMap { it.getAllReferencedExternalTypes() }
            }

        (externalTypesFromCallableMembers + externalTypesFromClasses).distinct()
    }

    val allReferencedExternalTypesWithoutBuiltInModules: List<ExternalType> =
        allReferencedExternalTypes.filter { it.module !in builtInModules }
}

private object ReferencedTypesVisitor : KotlinDirectlyCallableMemberSwiftModelVisitor<List<SirType>> {

    override fun visit(function: KotlinFunctionSwiftModel): List<SirType> =
        function.valueParameters.map { it.type } + function.returnType

    override fun visit(regularProperty: KotlinRegularPropertySwiftModel): List<SirType> =
        listOf(regularProperty.type)
}

private fun SwiftIrDeclaration.getAllReferencedExternalTypes(): List<ExternalType> =
    when (this) {
        is SwiftIrTypeDeclaration.External -> {
            (typeParameters + superTypes).flatMap { it.getAllReferencedExternalTypes() } +
                    ExternalType.Class(module.name, name, typeParameters.size)
        }
        is SwiftIrTypeDeclaration.Local -> (typeParameters + superTypes).flatMap { it.getAllReferencedExternalTypes() }
        is SwiftIrProtocolDeclaration.External -> listOf(ExternalType.Protocol(module.name, name)) + superTypes.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftIrProtocolDeclaration.Local -> superTypes.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftIrTypeParameterDeclaration -> bounds.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftIrModule -> emptyList()
        BuiltinDeclarations.Any -> emptyList()
        BuiltinDeclarations.AnyClass -> emptyList()
        BuiltinDeclarations.Protocol -> emptyList()
        BuiltinDeclarations.Void -> emptyList()
    }

private fun SirType.getAllReferencedExternalTypes(): List<ExternalType> =
    when (this) {
        is SwiftPointerSirType -> pointee.getAllReferencedExternalTypes()
        is SwiftClassSirType -> declaration.getAllReferencedExternalTypes() + typeArguments.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftGenericTypeUsageSirType -> declaration.getAllReferencedExternalTypes()
        is SwiftLambdaSirType -> returnType.getAllReferencedExternalTypes() + parameterTypes.flatMap { it.getAllReferencedExternalTypes() }
        is SwiftProtocolSirType -> declaration.getAllReferencedExternalTypes()
        is SwiftNullableReferenceSirType -> nonNullType.getAllReferencedExternalTypes()
        is SwiftPrimitiveSirType -> emptyList()
        ObjcProtocolSirType -> emptyList()
        SwiftAnyHashableSirType -> emptyList()
        SwiftAnyObjectSirType -> emptyList()
        SwiftAnySirType -> emptyList()
        SwiftErrorSirType -> emptyList()
        SwiftInstanceSirType -> emptyList()
        SwiftMetaClassSirType -> emptyList()
        SwiftVoidSirType -> emptyList()
    }

