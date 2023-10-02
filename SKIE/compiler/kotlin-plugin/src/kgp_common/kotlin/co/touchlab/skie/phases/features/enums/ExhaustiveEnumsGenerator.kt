package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.features.suspend.SuspendGenerator
import co.touchlab.skie.phases.features.suspend.addAvailabilityForAsync
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirValueParameter
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
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass

object ExhaustiveEnumsGenerator : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        exposedClasses
            .filter { it.isSupported }
            .forEach {
                generate(it)
            }
    }

    context(SkiePhase.Context)
    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.classDescriptor.kind.isEnumClass &&
                this.classDescriptor.isEnumInteropEnabled

    context(SkiePhase.Context)
    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = this.getConfiguration(EnumInterop.Enabled)

    context(SirPhase.Context)
    private fun generate(classSwiftModel: MutableKotlinClassSwiftModel) {
        val skieClass = classSwiftModel.generateBridge()

        classSwiftModel.configureBridging(skieClass)
    }

    object FunctionGeneratorPhase : StatefulSirPhase()
}

private fun MutableKotlinClassSwiftModel.configureBridging(skieClass: SirClass) {
    bridgedSirClass = skieClass

    visibility = SwiftModelVisibility.Replaced
}

context(SirPhase.Context)
private fun KotlinClassSwiftModel.generateBridge(): SirClass {
    val skieClass = createBridgingEnum()

    addConversionExtensions(skieClass)

    return skieClass
}

context(SirPhase.Context)
private fun KotlinClassSwiftModel.createBridgingEnum(): SirClass =
    SirClass(
        simpleName = kotlinSirClass.simpleName,
        parent = kotlinSirClass.namespace?.let { namespace ->
            SirExtension(
                classDeclaration = when (namespace) {
                    is SirClass -> namespace
                    is SirExtension -> namespace.classDeclaration
                },
                parent = sirProvider.getFile(this),
            )
        } ?: sirProvider.getFile(this),
        kind = SirClass.Kind.Enum,
    ).apply {
        internalTypeAlias = SirTypeAlias(
            simpleName = "__Enum",
            parent = sirProvider.getSkieNamespace(this@createBridgingEnum),
        ) {
            defaultType.also { it.useInternalName = false }
        }

        addEnumCases()
        addNestedClassTypeAliases()

        superTypes += listOf(
            sirBuiltins.Swift.Hashable.defaultType,
            sirBuiltins.Swift.CaseIterable.defaultType,
            sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
        )

        attributes.add("frozen")

        addCompanionObjectPropertyIfNeeded()
        addObjcBridgeableImplementation(this@createBridgingEnum)

        doInPhase(ExhaustiveEnumsGenerator.FunctionGeneratorPhase) {
            addPassthroughForMembers()
        }
    }

context(KotlinClassSwiftModel)
private fun SirClass.addEnumCases() {
    enumEntries.forEach {
        SirEnumCase(
            simpleName = it.identifier,
        )
    }
}

context(KotlinClassSwiftModel)
private fun SirClass.addNestedClassTypeAliases() {
    nestedClasses.forEach { nestedClass ->
        SirTypeAlias(
            simpleName = nestedClass.primarySirClass.fqName.simpleName,
        ) {
            DeclaredSirType(nestedClass.primarySirClass)
        }
    }
}

context(SirPhase.Context, KotlinClassSwiftModel)
private fun SirClass.addPassthroughForMembers() {
    allAccessibleDirectlyCallableMembers
        .forEach {
            it.accept(MemberPassthroughGeneratorVisitor(this))
        }
}

context(KotlinClassSwiftModel)
private fun SirClass.addCompanionObjectPropertyIfNeeded() {
    val companion = companionObject ?: return

    SirProperty(
        name = "companion",
        type = companion.primarySirClass.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirGetter().swiftPoetBuilderModifications.add {
            addStatement("return _ObjectiveCType.companion")
        }
    }
}

context(SwiftModelScope)
private fun KotlinClassSwiftModel.addConversionExtensions(skieClass: SirClass) {
    sirProvider.getFile(this).apply {
        addToKotlinConversionExtension(skieClass)
        addToSwiftConversionExtension(skieClass)
    }
}

context(KotlinClassSwiftModel)
private fun SirFile.addToKotlinConversionExtension(skieClass: SirClass) {
    SirExtension(
        classDeclaration = skieClass,
    ).apply {
        addToKotlinConversionMethod()
    }
}

context(KotlinClassSwiftModel)
private fun SirExtension.addToKotlinConversionMethod() {
    SirFunction(
        identifier = "toKotlinEnum",
        returnType = kotlinSirClass.defaultType,
    ).apply {
        swiftPoetBuilderModifications.add {
            addStatement("return _bridgeToObjectiveC()")
        }
    }
}

context(KotlinClassSwiftModel)
private fun SirFile.addToSwiftConversionExtension(skieClass: SirClass) {
    SirExtension(
        classDeclaration = kotlinSirClass,
    ).apply {
        addToSwiftConversionMethod(skieClass)
    }
}

context(KotlinClassSwiftModel)
private fun SirExtension.addToSwiftConversionMethod(skieClass: SirClass) {
    // TODO After Sir: solve name collision
    SirFunction(
        identifier = "toSwiftEnum",
        returnType = skieClass.defaultType,
    ).apply {
        swiftPoetBuilderModifications.add {
            addStatement("return %T._unconditionallyBridgeFromObjectiveC(self)", skieClass.defaultType.toSwiftPoetTypeName())
        }
    }
}

context(SirPhase.Context)
private class MemberPassthroughGeneratorVisitor(
    private val enum: SirClass,
) : KotlinDirectlyCallableMemberSwiftModelVisitor.Unit {

    override fun visit(function: KotlinFunctionSwiftModel) {
        if (!function.isSupported) return

        if (SuspendGenerator.hasSuspendWrapper(function)) {
            val asyncFunction = function.asyncSwiftModelOrNull ?: error("Suspend function must have an async swift model: $function")

            addFunction(asyncFunction)
        }

        addFunction(function)
    }

    private val unsupportedFunctionNames = listOf("compareTo(other:)", "hash()", "description()", "isEqual(_:)")

    private val KotlinFunctionSwiftModel.isSupported: Boolean
        get() = this.name !in unsupportedFunctionNames

    private fun addFunction(function: KotlinFunctionSwiftModel) {
        SirFunction(
            identifier = function.identifier,
            parent = enum,
            returnType = function.returnType,
        ).apply {
            addFunctionValueParameters(function)

            throws = function.isThrowing

            isAsync = function.isSuspend
            if (function.isSuspend) {
                addAvailabilityForAsync()
            }

            addFunctionBody(function)
        }
    }

    private fun SirFunction.addFunctionValueParameters(function: KotlinFunctionSwiftModel) {
        function.valueParameters.forEach {
            this.addFunctionValueParameter(it)
        }
    }

    private fun SirFunction.addFunctionValueParameter(valueParameter: KotlinValueParameterSwiftModel) {
        SirValueParameter(
            label = valueParameter.argumentLabel,
            name = valueParameter.parameterName,
            type = valueParameter.type,
        )
    }

    private fun SirFunction.addFunctionBody(function: KotlinFunctionSwiftModel) {
        this.addFunctionBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L(self as _ObjectiveCType).%N(%L)",
                if (function.isThrowing) "try " else "",
                if (function.isSuspend) "await " else "",
                function.reference,
                function.valueParameters.map { CodeBlock.of("%N", it.parameterName) }.joinToCode(", "),
            )
        }
    }

    override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
        SirProperty(
            name = regularProperty.identifier,
            parent = enum,
            type = regularProperty.type,
        ).apply {
            addGetter(regularProperty)
            addSetterIfPresent(regularProperty)
        }
    }

    private fun SirProperty.addGetter(regularProperty: KotlinRegularPropertySwiftModel) {
        SirGetter().addGetterBody(regularProperty)
    }

    private fun SirGetter.addGetterBody(regularProperty: KotlinRegularPropertySwiftModel) {
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "return %L(self as _ObjectiveCType).%N",
                if (regularProperty.getter.isThrowing) "try " else "",
                regularProperty.reference,
            )
        }
    }

    private fun SirProperty.addSetterIfPresent(regularProperty: KotlinRegularPropertySwiftModel) {
        val setter = regularProperty.setter ?: return

        SirSetter(
            modifiers = listOf(Modifier.NONMUTATING),
        ).addSetterBody(regularProperty, setter)
    }

    private fun SirSetter.addSetterBody(
        regularProperty: KotlinRegularPropertySwiftModel,
        setter: KotlinRegularPropertySetterSwiftModel,
    ) {
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "%L(self as _ObjectiveCType).%N = value",
                if (setter.isThrowing) "try " else "",
                regularProperty.reference,
            )
        }
    }
}
