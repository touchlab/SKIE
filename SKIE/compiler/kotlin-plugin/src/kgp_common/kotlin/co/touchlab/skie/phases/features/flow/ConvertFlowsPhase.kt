package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.kir.element.classDescriptorOrError
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.ErrorOutKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SuspendCompletionKirType
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.replaceArguments
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.model.TypeArgumentMarker

class ConvertFlowsPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allClasses.forEach {
            it.convertFlows()
        }
    }

    private fun KirClass.convertFlows() {
        convertFlowsInSuperTypes()

        convertFlowsInCallableDeclarations()
    }

    private fun KirClass.convertFlowsInSuperTypes() {
        superTypes.replaceAll {
            it.substituteFlows(configuration.flowMappingStrategy.limitedToTypeArguments())
        }
    }

    private fun KirClass.convertFlowsInCallableDeclarations() {
        callableDeclarations.forEach {
            it.convertFlows()
        }
    }

    private fun KirCallableDeclaration<*>.convertFlows() {
        when (this) {
            is KirConstructor -> convertFlows()
            is KirSimpleFunction -> convertFlows()
            is KirProperty -> convertFlows()
        }
    }

    private fun KirConstructor.convertFlows() {
        convertFlowsInValueParameters()
    }

    private fun KirSimpleFunction.convertFlows() {
        convertFlowsInValueParameters()

        returnType = returnType.substituteFlows(configuration.flowMappingStrategy)
    }

    private fun KirProperty.convertFlows() {
        type = type.substituteFlows(configuration.flowMappingStrategy)
    }

    private fun KirFunction<*>.convertFlowsInValueParameters() {
        valueParameters.forEach {
            it.convertFlows()
        }
    }

    private fun KirValueParameter.convertFlows() {
        type = type.substituteFlows(configuration.flowMappingStrategy)
    }

    private fun KirType.substituteFlows(flowMappingStrategy: FlowMappingStrategy): KirType =
        when (this) {
            is BlockPointerKirType -> copy(kotlinType = kotlinType.substituteFlows(flowMappingStrategy))
            ErrorOutKirType -> ErrorOutKirType
            is OirBasedKirType -> this
            is ReferenceKirType -> substituteFlows(flowMappingStrategy)
            is SuspendCompletionKirType -> copy(kotlinType = kotlinType.substituteFlows(flowMappingStrategy))
        }

    private fun ReferenceKirType.substituteFlows(flowMappingStrategy: FlowMappingStrategy): ReferenceKirType =
        copy(kotlinType = kotlinType.substituteFlows(flowMappingStrategy))

    private fun KotlinType.substituteFlows(flowMappingStrategy: FlowMappingStrategy): KotlinType {
        val flowMappingStrategyForTypeArguments = flowMappingStrategy.forTypeArgumentsOf(this)

        return when (flowMappingStrategy) {
            FlowMappingStrategy.Full -> {
                val supportedFlow = SupportedFlow.from(this)

                supportedFlow?.createType(this, flowMappingStrategyForTypeArguments)
                    ?: this.withSubstitutedArgumentsForFlow(flowMappingStrategyForTypeArguments)
            }
            FlowMappingStrategy.TypeArgumentsOnly -> this.withSubstitutedArgumentsForFlow(flowMappingStrategyForTypeArguments)
            FlowMappingStrategy.None -> this
        }
    }

    private fun SupportedFlow.createType(originalType: KotlinType, flowMappingStrategyForTypeArguments: FlowMappingStrategy): KotlinType {
        val substitutedArguments = originalType.arguments.map { it.substituteFlows(flowMappingStrategyForTypeArguments) }

        val hasNullableTypeArgument = originalType.arguments.any { it.type.isNullable() }
        val flowVariant = if (hasNullableTypeArgument) this.optionalVariant else this.requiredVariant
        val substitute = flowVariant.getKotlinKirClass(kirProvider).classDescriptorOrError.defaultType

        return KotlinTypeFactory.simpleType(substitute, arguments = substitutedArguments).makeNullableAsSpecified(originalType.isNullable())
    }

    private fun KotlinType.withSubstitutedArgumentsForFlow(flowMappingStrategy: FlowMappingStrategy): KotlinType =
        replaceArguments { it.substituteFlows(flowMappingStrategy) } as KotlinType

    private fun TypeArgumentMarker.substituteFlows(flowMappingStrategy: FlowMappingStrategy): TypeProjection =
        when (this) {
            is TypeProjectionImpl -> {
                val substitutedType = type.substituteFlows(flowMappingStrategy)

                if (this.type != substitutedType) TypeProjectionImpl(projectionKind, substitutedType) else this
            }
            is TypeProjection -> this
            else -> error("Unsupported type argument $this.")
        }
}
