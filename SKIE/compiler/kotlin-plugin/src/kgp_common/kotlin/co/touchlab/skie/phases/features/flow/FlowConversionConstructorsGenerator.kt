package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType

class FlowConversionConstructorsGenerator(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val sirProvider = context.sirProvider
    private val sirBuiltins = context.sirBuiltins

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        val file = sirFileProvider.getIrFileFromSkieNamespace("FlowConversions")

        SupportedFlow.values().forEach {
            it.generateAllConversions(file)
        }
    }

    private fun SupportedFlow.generateAllConversions(file: SirIrFile) {
        requiredVariant.generateAllConversions(file)
        optionalVariant.generateAllConversions(file)
    }

    private fun SupportedFlow.Variant.generateAllConversions(file: SirIrFile) {
        generateAllKotlinClassConversions(this, file)
        generateAllSwiftClassConversions(this, file)
    }

    private fun generateAllKotlinClassConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        generateKotlinClassWithAnyObjectConversions(variant, file)
        generateKotlinClassWithBridgeableConversions(variant, file)
    }

    private fun generateAllSwiftClassConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        generateSwiftClassWithAnyObjectConversions(variant, file)
        generateSwiftClassWithBridgeableConversions(variant, file)
    }

    private fun generateKotlinClassWithAnyObjectConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        file.addConversions(variant) { from ->
            addSwiftToKotlinConversion(
                from,
                variant,
                sirBuiltins.Swift.AnyObject.defaultType,
            ) { it }
        }
    }

    private fun generateKotlinClassWithBridgeableConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        file.addConversions(variant) { from ->
            addSwiftToKotlinConversion(
                from,
                variant,
                sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
            ) { it.typeParameter(sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first()) }
        }
    }

    private fun generateSwiftClassWithAnyObjectConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        generateSwiftClassConversions(
            variant,
            file,
            sirBuiltins.Swift.AnyObject.defaultType,
        ) { flowTypeParameter ->
            val flowTypeArgument = flowTypeParameter.toTypeParameterUsage()

            addConversions(variant) { from -> addKotlinToSwiftConversion(from, flowTypeArgument) }
            addConversions(variant) { from -> addSwiftToSwiftConversion(from, flowTypeArgument) }
        }
    }

    private fun generateSwiftClassWithBridgeableConversions(variant: SupportedFlow.Variant, file: SirIrFile) {
        generateSwiftClassConversions(
            variant,
            file,
            sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
        ) { flowTypeParameter ->
            val bridgeableTypeParameter = sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first()

            val flowTypeArgument = flowTypeParameter.toTypeParameterUsage()
            val bridgeableTypeArgument = flowTypeArgument.typeParameter(bridgeableTypeParameter)

            addConversions(variant) { from -> addKotlinToSwiftConversion(from, bridgeableTypeArgument) }
            addConversions(variant) { from -> addSwiftToSwiftConversion(from, bridgeableTypeArgument) }
            addConversions(variant) { from -> addSwiftToSwiftConversion(from, flowTypeArgument) }
        }
    }

    private fun generateSwiftClassConversions(
        variant: SupportedFlow.Variant,
        file: SirIrFile,
        typeBound: SirType,
        bodyFactory: SirExtension.(SirTypeParameter) -> Unit,
    ) {
        SirExtension(
            classDeclaration = variant.swiftClass,
            parent = file,
        ).apply {
            val typeParameter = classDeclaration.typeParameters.first()

            SirConditionalConstraint(
                typeParameter = typeParameter,
                bounds = listOf(typeBound),
            )

            bodyFactory(typeParameter)
        }
    }

    private fun <T> T.addConversions(
        variant: SupportedFlow.Variant,
        conversionBuilder: T.(from: SupportedFlow.Variant) -> Unit,
    ): T =
        apply {
            variant.forEachChildVariant {
                conversionBuilder(this@addConversions, it)
            }
        }

    private inline fun SupportedFlow.Variant.forEachChildVariant(action: (SupportedFlow.Variant) -> Unit) {
        SupportedFlow.values()
            .flatMap { it.variants }
            .filter { it.isCastableTo(this) }
            .forEach(action)
    }

    private fun SirIrFile.addSwiftToKotlinConversion(
        from: SupportedFlow.Variant,
        to: SupportedFlow.Variant,
        typeBound: SirType,
        flowTypeArgumentFactory: (TypeParameterUsageSirType) -> SirType,
    ) {
        SirSimpleFunction(
            identifier = to.kotlinClass.baseName,
            returnType = sirBuiltins.Swift.Void.defaultType,
        ).apply {
            val typeParameter = SirTypeParameter(
                name = "T",
                bounds = listOf(typeBound),
            )

            val flowTypeArgument = flowTypeArgumentFactory(typeParameter.toTypeParameterUsage())
            returnType = to.kotlinClass.toType(flowTypeArgument)

            SirValueParameter(
                label = "_",
                name = "flow",
                type = from.swiftClass.toType(typeParameter.toTypeParameterUsage()),
            )

            bodyBuilder.add {
                addStatement("return %T(%L)", to.kotlinClass.defaultType.toSwiftPoetDeclaredTypeName(), "flow.delegate")
            }
        }
    }

    private fun SirExtension.addKotlinToSwiftConversion(from: SupportedFlow.Variant, flowTypeArgument: SirType) {
        SirConstructor(
            isConvenience = true,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "flow",
                type = from.kotlinClass.toType(flowTypeArgument),
            )

            bodyBuilder.add {
                addStatement("self.init(internal: %L)", "flow")
            }
        }
    }

    private fun SirExtension.addSwiftToSwiftConversion(from: SupportedFlow.Variant, flowTypeArgument: SirType) {
        SirConstructor(
            isConvenience = true,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "flow",
                type = from.swiftClass.toType(flowTypeArgument),
            )

            bodyBuilder.add {
                addStatement("self.init(internal: %L)", "flow.delegate")
            }
        }
    }

    private val SupportedFlow.Variant.kotlinClass: SirClass
        get() = this.getKotlinKirClass(kirProvider).originalSirClass

    private val SupportedFlow.Variant.swiftClass: SirClass
        get() = this.getSwiftClass(sirProvider)
}
