package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.generator.internal.enums.ObjCBridgeable.addObjcBridgeableImplementation
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal class ExhaustiveEnumsGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    private val reporter: Reporter,
) : BaseGenerator(skieContext, namespaceProvider) {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        module.configure {
            exposedClasses
                .filter { it.isSupported }
                .forEach {
                    generate(it)
                }
        }
    }

    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.classDescriptor.kind.isEnumClass &&
            this.classDescriptor.isEnumInteropEnabled

    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = getConfiguration(EnumInterop.Enabled)

    private fun generate(classSwiftModel: MutableKotlinClassSwiftModel) {
        if (classSwiftModel.enumEntries.isEmpty()) {
            reporter.report(
                Reporter.Severity.Warning,
                "Enum ${classSwiftModel.identifier} has no entries, no Swift enum will be generated. " +
                    "To silence this warning, add @EnumInterop.Disabled above the enum declaration.",
                classSwiftModel.classDescriptor,
            )
            return
        }

        val bridge = classSwiftModel.configureBridging()
        classSwiftModel.generateBridge(bridge)
    }

    private fun MutableKotlinClassSwiftModel.configureBridging(): ObjcSwiftBridge.FromSKIE {
        val localContainingDeclaration = this.containingType?.swiftIrDeclaration as? SwiftIrTypeDeclaration.Local
        val name = if (localContainingDeclaration != null) {
            "__" + localContainingDeclaration.publicName.nested(this.identifier).asString("__")
        } else {
            this.identifier
        }

        val bridge = ObjcSwiftBridge.FromSKIE(
            nestedTypealiasName = localContainingDeclaration?.internalName?.nested(this.identifier),
            declaration = SwiftIrTypeDeclaration.Local.SKIEGeneratedSwiftType(
                swiftName = name,
                superTypes = listOf(
                    BuiltinDeclarations.Swift.Hashable,
                )
            )
        )

        this.visibility = SwiftModelVisibility.Replaced
        this.bridge = bridge
        return bridge
    }

    private fun KotlinClassSwiftModel.generateBridge(bridge: ObjcSwiftBridge.FromSKIE) {
        val classSwiftModel = this

        module.generateCode(this) {
            addImport("Foundation")

            addBridgingEnum(classSwiftModel, bridge)
        }

        module.file(this.classDescriptor.fqNameSafe.asString() + "_conversions") {
            addConversionExtensions(classSwiftModel, bridge)
        }
    }

    private fun FileSpec.Builder.addBridgingEnum(
        classSwiftModel: KotlinClassSwiftModel,
        bridge: ObjcSwiftBridge.FromSKIE,
    ) {
        addType(classSwiftModel.generateBridgingEnum(bridge))

        bridge.nestedTypealiasName?.let { nestedTypealiasName ->
            addExtension(
                ExtensionSpec.builder(nestedTypealiasName.parent.toSwiftPoetName())
                    .addType(
                        TypeAliasSpec.builder(nestedTypealiasName.name, bridge.declaration.publicName.toSwiftPoetName()).build()
                    )
                    .build()
            )
        }
    }

    private fun KotlinClassSwiftModel.generateBridgingEnum(bridge: ObjcSwiftBridge.FromSKIE): TypeSpec =
        TypeSpec.enumBuilder(bridge.declaration.publicName.toSwiftPoetName())
            .addAttribute("frozen")
            .addModifiers(Modifier.PUBLIC)
            .addSuperType(STRING)
            .addSuperType(DeclaredTypeName.typeName("Swift.Hashable"))
            .addSuperType(DeclaredTypeName.typeName("Swift.CaseIterable"))
            .addEnumCases(this)
            .addPassthroughForMembers(this)
            .addNestedClassTypeAliases(this)
            .addCompanionObjectPropertyIfNeeded(this)
            .addObjcBridgeableImplementation(this)
            .build()

    private fun TypeSpec.Builder.addEnumCases(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.apply {
            classSwiftModel.enumEntries.forEach {
                addEnumCase(it.identifier)
            }
        }

    private fun TypeSpec.Builder.addPassthroughForMembers(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.apply {
            classSwiftModel.allAccessibleDirectlyCallableMembers
                .forEach {
                    it.accept(MemberPassthroughGeneratorVisitor(this))
                }
        }

    private fun TypeSpec.Builder.addNestedClassTypeAliases(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.apply {
            classSwiftModel.nestedClasses.forEach {
                addType(
                    // TODO: Should this be originalDeclaration or swiftIrDeclaration?
                    TypeAliasSpec.builder(it.identifier, it.nonBridgedDeclaration.internalName.toSwiftPoetName())
                        .addModifiers(Modifier.PUBLIC)
                        .build()
                )
            }
        }

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
                    .build()
            )
        }

        // TODO When implementing interfaces: Implement remaining methods if needed (at least compareTo(other:) and toString/description should be supportable)
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
                ).build()
            )

        private fun FunctionSpec.Builder.addFunctionBody(function: KotlinFunctionSwiftModel): FunctionSpec.Builder =
            this.addStatement(
                "return %L(self as _ObjectiveCType).%N(%L)",
                if (function.isThrowing) "try " else "",
                function.reference,
                function.valueParameters.map { CodeBlock.of("%N", it.parameterName) }.joinToCode(", "),
            )

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
            builder.addProperty(
                PropertySpec.builder(regularProperty.identifier, regularProperty.type.toSwiftPoetUsage())
                    .addModifiers(Modifier.PUBLIC)
                    .addGetter(regularProperty)
                    .addSetterIfPresent(regularProperty)
                    .build()
            )
        }

        private fun PropertySpec.Builder.addGetter(regularProperty: KotlinRegularPropertySwiftModel): PropertySpec.Builder =
            this.getter(
                FunctionSpec.getterBuilder()
                    .addStatement(
                        "return %L(self as _ObjectiveCType).%N",
                        if (regularProperty.getter.isThrowing) "try " else "",
                        regularProperty.reference,
                    )
                    .build()
            )

        private fun PropertySpec.Builder.addSetterIfPresent(regularProperty: KotlinRegularPropertySwiftModel): PropertySpec.Builder =
            this.apply {
                val setter = regularProperty.setter ?: return@apply

                setter(
                    FunctionSpec.setterBuilder()
                        .addModifiers(Modifier.NONMUTATING)
                        .addParameter("value", regularProperty.type.toSwiftPoetUsage())
                        .addStatement(
                            "%L(self as _ObjectiveCType).%N = value",
                            if (setter.isThrowing) "try " else "",
                            regularProperty.reference,
                        )
                        .build()
                )
            }
    }

    private fun TypeSpec.Builder.addCompanionObjectPropertyIfNeeded(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.apply {
            val companion = classSwiftModel.companionObject ?: return@apply

            addProperty(
                PropertySpec.builder("companion", companion.swiftIrDeclaration.internalName.toSwiftPoetName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .getter(
                        FunctionSpec.getterBuilder()
                            .addStatement("return _ObjectiveCType.companion")
                            .build()
                    )
                    .build()
            )
        }

    private fun FileSpec.Builder.addConversionExtensions(classSwiftModel: KotlinClassSwiftModel, bridge: ObjcSwiftBridge.FromSKIE) {
        addToKotlinConversionExtension(classSwiftModel, bridge)
        addToSwiftConversionExtension(classSwiftModel, bridge)
    }

    private fun FileSpec.Builder.addToKotlinConversionExtension(classSwiftModel: KotlinClassSwiftModel, bridge: ObjcSwiftBridge.FromSKIE) {
        addExtension(
            ExtensionSpec.builder(bridge.declaration.publicName.toSwiftPoetName())
                .addModifiers(Modifier.PUBLIC)
                .addToKotlinConversionMethod(classSwiftModel)
                .build()
        )
    }

    private fun ExtensionSpec.Builder.addToKotlinConversionMethod(classSwiftModel: KotlinClassSwiftModel): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                // TODO After Sir: solve name collision
                FunctionSpec.builder("toKotlinEnum")
                    .returns(classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
                    .addStatement("return _bridgeToObjectiveC()")
                    .build()
            )
    }

    private fun FileSpec.Builder.addToSwiftConversionExtension(classSwiftModel: KotlinClassSwiftModel, bridge: ObjcSwiftBridge.FromSKIE) {
        addExtension(
            ExtensionSpec.builder(classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
                .addModifiers(Modifier.PUBLIC)
                .addToSwiftConversionMethod(bridge)
                .build()
        )
    }

    private fun ExtensionSpec.Builder.addToSwiftConversionMethod(bridge: ObjcSwiftBridge.FromSKIE): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                // TODO After Sir: solve name collision
                FunctionSpec.builder("toSwiftEnum")
                    .returns(bridge.declaration.publicName.toSwiftPoetName())
                    .addStatement("return %T._unconditionallyBridgeFromObjectiveC(self)", bridge.declaration.publicName.toSwiftPoetName())
                    .build()
            )
    }
}
