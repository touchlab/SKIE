package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType
import co.touchlab.skie.swiftmodel.SwiftModelScope

object FlowConversionConstructorsGenerator : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        val file = sirProvider.getFile(SirFile.skieNamespace, "FlowConversions")

        SupportedFlow.values().forEach {
            it.generateAllConversions(file)
        }
    }
}

context (SwiftModelScope)
private fun SupportedFlow.generateAllConversions(file: SirFile) {
    requiredVariant.generateAllConversions(file)
    optionalVariant.generateAllConversions(file)
}

context (SwiftModelScope)
private fun SupportedFlow.Variant.generateAllConversions(file: SirFile) {
    generateAllKotlinClassConversions(this, file)
    generateAllSwiftClassConversions(this, file)
}

context (SwiftModelScope)
private fun generateAllKotlinClassConversions(variant: SupportedFlow.Variant, file: SirFile) {
    generateKotlinClassWithAnyObjectConversions(variant, file)
    generateKotlinClassWithBridgeableConversions(variant, file)
}

context (SwiftModelScope)
private fun generateAllSwiftClassConversions(variant: SupportedFlow.Variant, file: SirFile) {
    generateSwiftClassWithAnyObjectConversions(variant, file)
    generateSwiftClassWithBridgeableConversions(variant, file)
}

context (SwiftModelScope)
private fun generateKotlinClassWithAnyObjectConversions(variant: SupportedFlow.Variant, file: SirFile) {
    file.addConversions(variant) { from ->
        addSwiftToKotlinConversion(
            from,
            variant,
            sirBuiltins.Swift.AnyObject.defaultType,
        ) { it }
    }
}

context (SwiftModelScope)
private fun generateKotlinClassWithBridgeableConversions(variant: SupportedFlow.Variant, file: SirFile) {
    file.addConversions(variant) { from ->
        addSwiftToKotlinConversion(
            from,
            variant,
            sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
        ) { it.typeParameter(sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first()) }
    }
}

context (SwiftModelScope)
private fun generateSwiftClassWithAnyObjectConversions(variant: SupportedFlow.Variant, file: SirFile) {
    generateSwiftClassConversions(
        variant,
        file,
        sirBuiltins.Swift.AnyObject.defaultType,
    ) { it }
}

context (SwiftModelScope)
private fun generateSwiftClassWithBridgeableConversions(variant: SupportedFlow.Variant, file: SirFile) {
    generateSwiftClassConversions(
        variant,
        file,
        sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
    ) { it.typeParameter(sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first()) }
}

context (SwiftModelScope)
private fun generateSwiftClassConversions(
    variant: SupportedFlow.Variant,
    file: SirFile,
    typeBound: SirType,
    flowTypeArgumentFactory: (TypeParameterUsageSirType) -> SirType,
) {
    SirExtension(
        classDeclaration = variant.swiftFlowClass(),
        parent = file,
    ).apply {
        val typeParameter = classDeclaration.typeParameters.first()

        SirConditionalConstraint(
            typeParameter = typeParameter,
            bounds = listOf(typeBound),
        )

        val flowTypeArgument = flowTypeArgumentFactory(typeParameter.toTypeParameterUsage())

        addConversions(variant) { from -> addKotlinToSwiftConversion(from, flowTypeArgument) }
        addConversions(variant) { from -> addSwiftToSwiftConversion(from, flowTypeArgument) }
    }
}

context (SwiftModelScope)
private fun <T> T.addConversions(
    variant: SupportedFlow.Variant,
    conversionBuilder: context (SwiftModelScope) T.(from: SupportedFlow.Variant) -> Unit,
): T =
    apply {
        variant.forEachChildVariant {
            conversionBuilder(this@SwiftModelScope, this@addConversions, it)
        }
    }

private inline fun SupportedFlow.Variant.forEachChildVariant(action: (SupportedFlow.Variant) -> Unit) {
    SupportedFlow.values()
        .flatMap { it.variants }
        .filter { it.isCastableTo(this) }
        .forEach(action)
}

context (SwiftModelScope)
private fun SirFile.addSwiftToKotlinConversion(
    from: SupportedFlow.Variant,
    to: SupportedFlow.Variant,
    typeBound: SirType,
    flowTypeArgumentFactory: (TypeParameterUsageSirType) -> SirType,
) {
    SirFunction(
        identifier = to.kotlinFlowModel.kotlinSirClass.baseName,
        returnType = sirBuiltins.Swift.Void.defaultType,
    ).apply {
        val typeParameter = SirTypeParameter(
            name = "T",
            bounds = listOf(typeBound),
        )

        val flowTypeArgument = flowTypeArgumentFactory(typeParameter.toTypeParameterUsage())
        returnType = to.kotlinFlowModel.kotlinSirClass.toType(flowTypeArgument)

        SirValueParameter(
            label = "_",
            name = "flow",
            type = from.swiftFlowClass().toType(typeParameter.toTypeParameterUsage()),
        )

        swiftPoetBuilderModifications.add {
            addStatement("return %T(%L)", to.kotlinFlowModel.kotlinSirClass.defaultType.toSwiftPoetDeclaredTypeName(), "flow.delegate")
        }
    }
}

context (SwiftModelScope)
private fun SirExtension.addKotlinToSwiftConversion(from: SupportedFlow.Variant, flowTypeArgument: SirType) {
    SirConstructor(
        isConvenience = true,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "flow",
            type = from.kotlinFlowModel.kotlinSirClass.toType(flowTypeArgument),
        )

        swiftPoetBuilderModifications.add {
            addStatement("self.init(internal: %L)", "flow")
        }
    }
}

context (SwiftModelScope)
private fun SirExtension.addSwiftToSwiftConversion(from: SupportedFlow.Variant, flowTypeArgument: SirType) {
    SirConstructor(
        isConvenience = true,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "flow",
            type = from.swiftFlowClass().toType(flowTypeArgument),
        )

        swiftPoetBuilderModifications.add {
            addStatement("self.init(internal: %L)", "flow.delegate")
        }
    }
}
