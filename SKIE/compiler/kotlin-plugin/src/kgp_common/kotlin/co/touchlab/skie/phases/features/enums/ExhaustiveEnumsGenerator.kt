package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.ConfigurationContainer
import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySetterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.util.swift.addFunctionBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass

class ExhaustiveEnumsGenerator(
    override val context: SirPhase.Context,
) : SirPhase, ConfigurationContainer {

    context(SirPhase.Context)
    override fun execute() {
        exposedClasses
            .filter { it.isSupported }
            .forEach {
                generate(it)
            }
    }

    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.classDescriptor.kind.isEnumClass &&
            this.classDescriptor.isEnumInteropEnabled

    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = getConfiguration(EnumInterop.Enabled)

    context(SwiftModelScope)
    private fun generate(classSwiftModel: MutableKotlinClassSwiftModel) {
        val skieClass = classSwiftModel.generateBridge()

        classSwiftModel.configureBridging(skieClass)
    }
}

private fun MutableKotlinClassSwiftModel.configureBridging(skieClass: SirClass) {
    bridgedSirClass = skieClass

    visibility = SwiftModelVisibility.Replaced
}

context(SwiftModelScope)
private fun KotlinClassSwiftModel.generateBridge(): SirClass {
    val skieClass = createBridgingEnum()

    addConversionExtensions(skieClass)

    return skieClass
}

context(SwiftModelScope)
private fun KotlinClassSwiftModel.createBridgingEnum(): SirClass {
    val enum = SirClass(
        simpleName = kotlinSirClass.simpleName,
        parent = kotlinSirClass.namespace?.let { namespace ->
            SirExtension(
                typeDeclaration = when (namespace) {
                    is SirClass -> namespace
                    is SirExtension -> namespace.typeDeclaration
                },
                parent = sirProvider.getFile(this),
            )
        } ?: sirProvider.getFile(this),
        kind = SirClass.Kind.Enum,
    )

    enum.internalTypeAlias = SirTypeAlias(
        simpleName = "__Enum",
        parent = sirProvider.getSkieNamespace(this),
    ) {
        enum.defaultType.also { it.useInternalName = false }
    }

    addEnumCases(enum)
    addNestedClassTypeAliases(enum)

    enum.superTypes += listOf(
        sirBuiltins.Swift.Hashable.defaultType,
        sirBuiltins.Swift.CaseIterable.defaultType,
        sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
    )

    enum.swiftPoetBuilderModifications.add {
        addAttribute("frozen")
        addPassthroughForMembers()
        addCompanionObjectPropertyIfNeeded()
        addObjcBridgeableImplementation()
    }

    return enum
}

private fun KotlinClassSwiftModel.addEnumCases(skieClass: SirClass) {
    enumEntries.forEach {
        SirEnumCase(
            simpleName = it.identifier,
            parent = skieClass,
        )
    }
}

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addPassthroughForMembers() {
    allAccessibleDirectlyCallableMembers
        .forEach {
            it.accept(MemberPassthroughGeneratorVisitor(this))
        }
}

private fun KotlinClassSwiftModel.addNestedClassTypeAliases(skieClass: SirClass) {
    nestedClasses.forEach { nestedClass ->
        SirTypeAlias(
            simpleName = nestedClass.primarySirClass.fqName.simpleName,
            parent = skieClass,
        ) {
            DeclaredSirType(nestedClass.primarySirClass)
        }
    }
}

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addCompanionObjectPropertyIfNeeded() {
    val companion = companionObject ?: return

    addProperty(
        PropertySpec.builder("companion", companion.primarySirClass.internalName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .getter(
                FunctionSpec.getterBuilder()
                    .addStatement("return _ObjectiveCType.companion")
                    .build(),
            )
            .build(),
    )
}

context(SwiftModelScope)
private fun KotlinClassSwiftModel.addConversionExtensions(skieClass: SirClass) {
    sirProvider.getFile(this).swiftPoetBuilderModifications.add {
        addToKotlinConversionExtension(skieClass)
        addToSwiftConversionExtension(skieClass)
    }
}

context(KotlinClassSwiftModel)
private fun FileSpec.Builder.addToKotlinConversionExtension(skieClass: SirClass) {
    addExtension(
        ExtensionSpec.builder(skieClass.fqName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC)
            .addToKotlinConversionMethod()
            .build(),
    )
}

context(KotlinClassSwiftModel)
private fun ExtensionSpec.Builder.addToKotlinConversionMethod(): ExtensionSpec.Builder =
    addFunction(
        // TODO After Sir: solve name collision
        FunctionSpec.builder("toKotlinEnum")
            .returns(kotlinSirClass.internalName.toSwiftPoetName())
            .addStatement("return _bridgeToObjectiveC()")
            .build(),
    )

context(KotlinClassSwiftModel)
private fun FileSpec.Builder.addToSwiftConversionExtension(skieClass: SirClass) {
    addExtension(
        ExtensionSpec.builder(kotlinSirClass.internalName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC)
            .addToSwiftConversionMethod(skieClass)
            .build(),
    )
}

context(KotlinClassSwiftModel)
private fun ExtensionSpec.Builder.addToSwiftConversionMethod(skieClass: SirClass): ExtensionSpec.Builder =
    addFunction(
        // TODO After Sir: solve name collision
        FunctionSpec.builder("toSwiftEnum")
            .returns(skieClass.internalName.toSwiftPoetName())
            .addStatement("return %T._unconditionallyBridgeFromObjectiveC(self)", skieClass.internalName.toSwiftPoetName())
            .build(),
    )

private class MemberPassthroughGeneratorVisitor(
    private val builder: TypeSpec.Builder,
) : KotlinDirectlyCallableMemberSwiftModelVisitor.Unit {

    override fun visit(function: KotlinFunctionSwiftModel) {
        if (!function.isSupported) return

        builder.addFunction(
            FunctionSpec.builder(function.identifier)
                .addModifiers(Modifier.PUBLIC)
                .addFunctionValueParameters(function)
                .throws(function.isThrowing)
                .returns(function.returnType.toSwiftPoetUsage())
                .addFunctionBody(function)
                .build(),
        )
    }

    private val unsupportedFunctionNames = listOf("compareTo(other:)", "hash()", "description()", "isEqual(_:)")

    private val KotlinFunctionSwiftModel.isSupported: Boolean
        get() = !this.isSuspend && this.name !in unsupportedFunctionNames

    private fun FunctionSpec.Builder.addFunctionValueParameters(function: KotlinFunctionSwiftModel): FunctionSpec.Builder =
        this.apply {
            function.valueParameters.forEach {
                this.addFunctionValueParameter(it)
            }
        }

    private fun FunctionSpec.Builder.addFunctionValueParameter(valueParameter: KotlinValueParameterSwiftModel): FunctionSpec.Builder =
        this.addParameter(
            ParameterSpec.builder(
                valueParameter.argumentLabel,
                valueParameter.parameterName,
                valueParameter.type.toSwiftPoetUsage(),
            ).build(),
        )

    private fun FunctionSpec.Builder.addFunctionBody(function: KotlinFunctionSwiftModel): FunctionSpec.Builder =
        this.addFunctionBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L(self as _ObjectiveCType).%N(%L)",
                if (function.isThrowing) "try " else "",
                function.reference,
                function.valueParameters.map { CodeBlock.of("%N", it.parameterName) }.joinToCode(", "),
            )
        }

    override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
        builder.addProperty(
            PropertySpec.builder(regularProperty.identifier, regularProperty.type.toSwiftPoetUsage())
                .addModifiers(Modifier.PUBLIC)
                .addGetter(regularProperty)
                .addSetterIfPresent(regularProperty)
                .build(),
        )
    }

    private fun PropertySpec.Builder.addGetter(regularProperty: KotlinRegularPropertySwiftModel): PropertySpec.Builder =
        this.getter(
            FunctionSpec.getterBuilder()
                .addGetterBody(regularProperty)
                .build(),
        )

    private fun FunctionSpec.Builder.addGetterBody(regularProperty: KotlinRegularPropertySwiftModel): FunctionSpec.Builder =
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "return %L(self as _ObjectiveCType).%N",
                if (regularProperty.getter.isThrowing) "try " else "",
                regularProperty.reference,
            )
        }

    private fun PropertySpec.Builder.addSetterIfPresent(regularProperty: KotlinRegularPropertySwiftModel): PropertySpec.Builder =
        this.apply {
            val setter = regularProperty.setter ?: return@apply

            setter(
                FunctionSpec.setterBuilder()
                    .addModifiers(Modifier.NONMUTATING)
                    .addParameter("value", regularProperty.type.toSwiftPoetUsage())
                    .addSetterBody(regularProperty, setter)
                    .build(),
            )
        }

    private fun FunctionSpec.Builder.addSetterBody(
        regularProperty: KotlinRegularPropertySwiftModel,
        setter: KotlinRegularPropertySetterSwiftModel,
    ): FunctionSpec.Builder =
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "%L(self as _ObjectiveCType).%N = value",
                if (setter.isThrowing) "try " else "",
                regularProperty.reference,
            )
        }
}