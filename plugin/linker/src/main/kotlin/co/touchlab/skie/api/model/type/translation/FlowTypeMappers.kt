package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.isNullable

object FlowTypeMappers {

    context(SwiftModelScope)
    fun getMapperOrNull(type: KotlinType): FlowTypeMapper? {
        val supportedFlow = SupportedFlow.from(type) ?: return null

        val targetFlowSwiftModel = TargetFlowSwiftModel(
            nonOptional = referenceClass(supportedFlow.toNonOptionalFqName),
            optional = referenceClass(supportedFlow.toOptionalFqName),
        )

        return FlowMapper(targetFlowSwiftModel)
    }

    private class FlowMapper(private val targetFlowSwiftModel: TargetFlowSwiftModel) : FlowTypeMapper {

        context(SwiftModelScope)
        override fun mapType(
            type: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
        ): SwiftNonNullReferenceTypeModel {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> {
                    val typeArguments = type.arguments.map {
                        translator.mapReferenceTypeIgnoringNullability(it.type, swiftExportScope)
                    }

                    SwiftKotlinTypeClassTypeModel(targetFlowSwiftModel.nonOptional, typeArguments)
                }
                else -> {
                    val hasNullableTypeArgument = type.arguments.any { it.type.isNullable() }

                    val skieFlow = if (hasNullableTypeArgument) targetFlowSwiftModel.optional else targetFlowSwiftModel.nonOptional

                    val skieFlowType = KotlinTypeFactory.simpleType(
                        skieFlow.classDescriptor.defaultType,
                        arguments = type.arguments,
                    )

                    translator.mapReferenceTypeIgnoringNullabilitySkippingPredefined(skieFlowType, swiftExportScope)
                }
            }
        }
    }

    private data class TargetFlowSwiftModel(val nonOptional: KotlinClassSwiftModel, val optional: KotlinClassSwiftModel)
}
