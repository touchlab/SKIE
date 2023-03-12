package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy

internal class FlowConversionConstructorsGenerator(
    private val skieContext: SkieContext,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = SkieFeature.SuspendInterop in configuration.enabledFeatures &&
        SkieFeature.SwiftRuntime in configuration.enabledFeatures

    override fun runObjcPhase() {
        skieContext.module.file("SkieFlowConversions") {
            SupportedFlow.values().forEach {
                it.generateConversions()
            }
        }
    }
}

context (FileSpec.Builder, SwiftModelScope)
private fun SupportedFlow.generateConversions() {
    requiredVariant.generateConversions()
    optionalVariant.generateConversions()
}

context (FileSpec.Builder, SwiftModelScope)
private fun SupportedFlow.Variant.generateConversions() {
    generateKotlinClassConversions(this)
    generateSwiftClassConversions(this)
}

context (SwiftModelScope)
private fun FileSpec.Builder.generateKotlinClassConversions(variant: SupportedFlow.Variant) {
    addExtension(
        ExtensionSpec.builder(variant.kotlinFlowModel.nonBridgedDeclaration.publicName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC)
            .addConversions(variant) { index, from -> addSwiftToKotlinConversion(index, from) }
            .build()
    )
}

context (SwiftModelScope)
private fun FileSpec.Builder.generateSwiftClassConversions(variant: SupportedFlow.Variant) {
    addExtension(
        ExtensionSpec.builder(variant.swiftFlowDeclaration.publicName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC)
            .addConversions(variant) { _, from -> addKotlinToSwiftConversion(from) }
            .addConversions(variant) { _, from -> addSwiftToSwiftConversion(from) }
            .build()
    )
}

context (SwiftModelScope)
private fun ExtensionSpec.Builder.addConversions(
    variant: SupportedFlow.Variant,
    conversionBuilder: context (SwiftModelScope) ExtensionSpec.Builder.(index: Int, from: SupportedFlow.Variant) -> Unit,
): ExtensionSpec.Builder =
    apply {
        variant.forEachChildVariant { index, value ->
            conversionBuilder(this@SwiftModelScope, this@addConversions, index, value)
        }
    }

private inline fun SupportedFlow.Variant.forEachChildVariant(action: (Int, SupportedFlow.Variant) -> Unit) {
    SupportedFlow.values()
        .flatMap { it.variants }
        .filter { it.isCastableTo(this) }
        .forEachIndexed(action)
}

context (SwiftModelScope)
private fun ExtensionSpec.Builder.addSwiftToKotlinConversion(index: Int, from: SupportedFlow.Variant) {
    addFunction(
        FunctionSpec.constructorBuilder()
            .addModifiers(Modifier.CONVENIENCE)
            .addAttribute(AttributeSpec.builder("objc(initWithO${index}:)").build())
            .addParameter("_", "flow", from.swiftFlowDeclaration.internalName.toSwiftPoetName().parameterizedBy(TypeVariableName("T")))
            .addStatement("self.init(%L)", "flow.delegate")
            .build()
    )
}

private fun ExtensionSpec.Builder.addSwiftToSwiftConversion(from: SupportedFlow.Variant) {
    addFunction(
        FunctionSpec.constructorBuilder()
            .addModifiers(Modifier.CONVENIENCE)
            .addParameter("_", "flow", from.swiftFlowDeclaration.internalName.toSwiftPoetName().parameterizedBy(TypeVariableName("T")))
            .addStatement("self.init(internal: %L)", "flow.delegate")
            .build()
    )
}

context (SwiftModelScope)
private fun ExtensionSpec.Builder.addKotlinToSwiftConversion(from: SupportedFlow.Variant) {
    addFunction(
        FunctionSpec.constructorBuilder()
            .addModifiers(Modifier.CONVENIENCE)
            .addParameter("_", "flow", from.kotlinFlowModel.nonBridgedDeclaration.internalName.toSwiftPoetName().parameterizedBy(TypeVariableName("T")))
            .addStatement("self.init(internal: %L)", "flow")
            .build()
    )
}
