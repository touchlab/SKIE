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

        return FlowMapper(
            nonOptionalFlow = referenceClass(supportedFlow.toNonOptionalFqName),
            optionalFlow = referenceClass(supportedFlow.toOptionalFqName),
        )
    }

    private class FlowMapper(val nonOptionalFlow: KotlinClassSwiftModel, val optionalFlow: KotlinClassSwiftModel) : FlowTypeMapper {

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

                    SwiftKotlinTypeClassTypeModel(nonOptionalFlow, typeArguments)
                }
                else -> {
                    val hasNullableTypeArgument = type.arguments.any { it.type.isNullable() }

                    val skieFlow = if (hasNullableTypeArgument) optionalFlow else nonOptionalFlow

                    val skieFlowType = KotlinTypeFactory.simpleType(
                        skieFlow.classDescriptor.defaultType,
                        arguments = type.arguments,
                    )

                    translator.mapReferenceTypeIgnoringNullabilitySkippingPredefined(skieFlowType, swiftExportScope)
                }
            }
        }
    }
}
