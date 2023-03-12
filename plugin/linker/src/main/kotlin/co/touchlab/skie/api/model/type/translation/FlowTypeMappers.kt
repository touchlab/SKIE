package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceSirType
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.isNullable

object FlowTypeMappers {

    context(SwiftModelScope)
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
        ): SwiftNonNullReferenceSirType {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> {
                    val typeArguments = type.arguments.map {
                        translator.mapReferenceTypeIgnoringNullability(it.type, swiftExportScope, FlowMappingStrategy.Full)
                    }
                    val hasNullableTypeArgument = type.arguments.any { it.type.isNullable() }
                    val flowVariant = if (hasNullableTypeArgument) supportedFlow.optionalVariant else supportedFlow.requiredVariant

                    SwiftClassSirType(flowVariant.kotlinFlowModel.nonBridgedDeclaration, typeArguments)
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
                        FlowMappingStrategy.Full,
                    )
                }
            }
        }
    }
}
