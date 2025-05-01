package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.common.FlowMappingStrategy
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.DeclarationBackedKirType
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.NullableReferenceKirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.PointerKirType
import co.touchlab.skie.kir.type.SpecialOirKirType
import co.touchlab.skie.kir.type.TypeParameterUsageKirType
import co.touchlab.skie.kir.type.UnresolvedFlowKirType
import co.touchlab.skie.phases.KirPhase

class ConvertFlowsPhase(context: KirPhase.Context) : KirPhase {

    private val kirProvider = context.kirProvider

    context(KirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach {
            it.convertFlows()
        }
    }

    private fun KirClass.convertFlows() {
        convertFlowsInSuperTypes()

        convertFlowsInCallableDeclarations()
    }

    private fun KirClass.convertFlowsInSuperTypes() {
        configuration.flowMappingStrategy.limitFlowMappingToTypeArguments().run {
            superTypes.replaceAll {
                it.substituteFlows()
            }
        }
    }

    private fun KirClass.convertFlowsInCallableDeclarations() {
        callableDeclarations.forEach {
            it.convertFlows()
        }
    }

    private fun KirCallableDeclaration<*>.convertFlows() {
        configuration.flowMappingStrategy.run {
            when (this@convertFlows) {
                is KirConstructor -> convertFlows()
                is KirSimpleFunction -> convertFlows()
                is KirProperty -> convertFlows()
            }
        }
    }

    private fun KirConstructor.convertFlows() {
        convertFlowsInValueParameters()
    }

    context(FlowMappingStrategy)
    private fun KirSimpleFunction.convertFlows() {
        convertFlowsInValueParameters()

        returnType = returnType.substituteFlows()
    }

    context(FlowMappingStrategy)
    private fun KirProperty.convertFlows() {
        type = type.substituteFlows()
    }

    private fun KirFunction<*>.convertFlowsInValueParameters() {
        valueParameters.forEach {
            it.convertFlows()
        }
    }

    private fun KirValueParameter.convertFlows() {
        configuration.flowMappingStrategy.run {
            type = type.substituteFlows()
        }
    }

    context(FlowMappingStrategy)
    private fun KirType.substituteFlows(): KirType = when (this) {
        is NonNullReferenceKirType -> substituteFlows()
        is NullableReferenceKirType -> copy(nonNullType = nonNullType.substituteFlows())
        is OirBasedKirType -> this
        is PointerKirType -> copy(pointee = pointee.substituteFlows())
    }

    context(FlowMappingStrategy)
    private fun NonNullReferenceKirType.substituteFlows(): NonNullReferenceKirType = when (this) {
        is BlockPointerKirType -> copy(
            valueParameterTypes = valueParameterTypes.map { it.substituteFlows() },
            returnType = returnType.substituteFlows(),
        )
        is DeclarationBackedKirType -> substituteFlows()
        is SpecialOirKirType -> this
        is TypeParameterUsageKirType -> this
    }

    context(FlowMappingStrategy)
    private fun DeclarationBackedKirType.substituteFlows(): DeclaredKirType = when (this) {
        is DeclaredKirType -> {
            declaration.withFlowMappingForTypeArguments {
                copy(typeArguments = typeArguments.map { it.substituteFlows() })
            }
        }
        is UnresolvedFlowKirType -> resolve()
    }

    context(FlowMappingStrategy)
    private fun UnresolvedFlowKirType.resolve(): DeclaredKirType = when (this@FlowMappingStrategy) {
        FlowMappingStrategy.Full -> toSkieFlowType()
        FlowMappingStrategy.TypeArgumentsOnly, FlowMappingStrategy.None -> toCoroutinesFlowType()
    }

    context(FlowMappingStrategy)
    private fun UnresolvedFlowKirType.toSkieFlowType(): DeclaredKirType {
        val kirClass = flowType.getKotlinKirClass(kirProvider)

        val typeArgument = kirClass.withFlowMappingForTypeArguments {
            evaluateFlowTypeArgument().substituteFlows()
        }

        return DeclaredKirType(
            declaration = kirClass,
            typeArguments = listOf(typeArgument),
        )
    }

    private fun UnresolvedFlowKirType.toCoroutinesFlowType(): DeclaredKirType = DeclaredKirType(
        declaration = flowType.getCoroutinesKirClass(kirProvider),
        typeArguments = emptyList(),
    )
}
