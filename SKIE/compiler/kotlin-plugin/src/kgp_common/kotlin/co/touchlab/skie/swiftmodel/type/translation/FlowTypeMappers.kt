package co.touchlab.skie.swiftmodel.type.translation

import co.touchlab.skie.phases.features.flow.SupportedFlow
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.NonNullSirType
import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.isNullable

object FlowTypeMappers {

    fun getMapperOrNull(type: KotlinType): FlowTypeMapper? {
        val supportedFlow = SupportedFlow.from(type) ?: return null

        return FlowMapper(supportedFlow)
    }

    private class FlowMapper(val supportedFlow: SupportedFlow) : FlowTypeMapper {

        context(SwiftModelScope)
        override fun mapType(
            type: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> {
                    val typeArguments = type.arguments.map {
                        translator.mapReferenceTypeIgnoringNullability(
                            it.type,
                            swiftExportScope,
                            flowMappingStrategy.forTypeArgumentsOf(type),
                        )
                    }
                    val hasNullableTypeArgument = type.arguments.any { it.type.isNullable() }
                    val flowVariant = if (hasNullableTypeArgument) supportedFlow.optionalVariant else supportedFlow.requiredVariant

                    DeclaredSirType(flowVariant.kotlinFlowModel.kotlinSirClass, typeArguments)
                }
                else -> {
                    val hasNullableTypeArgument = type.arguments.any { it.type.isNullable() }

                    val flowVariant = if (hasNullableTypeArgument) supportedFlow.optionalVariant else supportedFlow.requiredVariant

                    val skieFlowType = KotlinTypeFactory.simpleType(
                        flowVariant.kotlinFlowModel.classDescriptor.defaultType,
                        arguments = type.arguments,
                    )

                    translator.mapReferenceTypeIgnoringNullabilitySkippingPredefined(
                        skieFlowType,
                        swiftExportScope,
                        flowMappingStrategy.forTypeArgumentsOf(type),
                    )
                }
            }
        }
    }
}
