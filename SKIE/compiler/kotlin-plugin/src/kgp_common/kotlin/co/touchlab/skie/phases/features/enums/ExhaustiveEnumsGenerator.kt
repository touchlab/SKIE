package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.features.suspend.SuspendGenerator
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.addAvailabilityForAsync
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
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.ActualKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
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

    object EnumBodyGeneratorPhase : StatefulSirPhase()
}

private fun MutableKotlinClassSwiftModel.configureBridging(skieClass: SirClass) {
    bridgedSirClass = skieClass

    kotlinSirClass.visibility = SirVisibility.PublicButReplaced
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
        baseName = kotlinSirClass.baseName,
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
            baseName = "Enum",
            parent = sirProvider.getSkieNamespace(this@createBridgingEnum),
            visibility = SirVisibility.PublicButReplaced,
        ) {
            defaultType.withFqName()
        }

        addEnumCases()

        superTypes += listOf(
            sirBuiltins.Swift.Hashable.defaultType,
            sirBuiltins.Swift.CaseIterable.defaultType,
            sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
        )

        attributes.add("frozen")

        doInPhase(ExhaustiveEnumsGenerator.EnumBodyGeneratorPhase) {
            addPassthroughForMembers()
            addNestedClassTypeAliases()
            addCompanionObjectPropertyIfNeeded()
            addObjcBridgeableImplementation(this@createBridgingEnum)
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

context(SirPhase.Context, KotlinClassSwiftModel)
private fun SirClass.addPassthroughForMembers() {
    allAccessibleDirectlyCallableMembers
        .forEach {
            it.accept(MemberPassthroughGeneratorVisitor(this))
        }
}

context(KotlinClassSwiftModel)
private fun SirClass.addNestedClassTypeAliases() {
    nestedClasses.forEach { nestedClass ->
        addNestedClassTypeAlias(nestedClass.kotlinSirClass)
        nestedClass.bridgedSirClass?.let { addNestedClassTypeAlias(it) }
    }
}

context(KotlinClassSwiftModel)
private fun SirClass.addNestedClassTypeAlias(sirClass: SirClass) {
    SirTypeAlias(
        baseName = sirClass.publicName.simpleName,
        visibility = sirClass.visibility,
    ) {
        sirClass.defaultType
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
        if (function !is ActualKotlinFunctionSwiftModel || !function.primarySirFunction.isSupported) return

        if (SuspendGenerator.hasSuspendWrapper(function)) {
            val asyncFunction = function.asyncSwiftModelOrNull ?: error("Suspend function must have an async swift model: $function")

            addFunction(asyncFunction)
        }

        addFunction(function)
    }

    private val unsupportedFunctionNames = listOf("compareTo(other:)", "hash()", "description()", "isEqual(_:)")

    private val SirFunction.isSupported: Boolean
        get() = this.name !in unsupportedFunctionNames

    private fun addFunction(function: KotlinFunctionSwiftModel) {
        SirFunction(
            identifier = function.primarySirFunction.identifier,
            parent = enum,
            returnType = function.primarySirFunction.returnType,
        ).apply {
            addFunctionValueParameters(function.primarySirFunction)

            throws = function.primarySirFunction.throws

            isAsync = function.primarySirFunction.isAsync
            if (function.primarySirFunction.isAsync) {
                addAvailabilityForAsync()
            }

            addFunctionBody(function)
        }
    }

    private fun SirFunction.addFunctionValueParameters(function: SirFunction) {
        function.valueParameters.forEach {
            this.addFunctionValueParameter(it)
        }
    }

    private fun SirFunction.addFunctionValueParameter(valueParameter: SirValueParameter) {
        SirValueParameter(
            label = valueParameter.label,
            name = valueParameter.name,
            type = valueParameter.type,
        )
    }

    private fun SirFunction.addFunctionBody(function: KotlinFunctionSwiftModel) {
        this.addFunctionBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L(self as _ObjectiveCType).%N(%L)",
                if (function.primarySirFunction.throws) "try " else "",
                if (function.primarySirFunction.isAsync) "await " else "",
                function.primarySirFunction.reference,
                function.primarySirFunction.valueParameters.map { CodeBlock.of("%N", it.name) }.joinToCode(", "),
            )
        }
    }

    override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
        SirProperty(
            identifier = regularProperty.primarySirProperty.identifier,
            parent = enum,
            type = regularProperty.primarySirProperty.type,
        ).apply {
            addGetter(regularProperty)
            addSetterIfPresent(regularProperty)
        }
    }

    private fun SirProperty.addGetter(regularProperty: KotlinRegularPropertySwiftModel) {
        SirGetter(
            throws = regularProperty.primarySirProperty.getter!!.throws,
        ).addGetterBody(regularProperty)
    }

    private fun SirGetter.addGetterBody(regularProperty: KotlinRegularPropertySwiftModel) {
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "return %L(self as _ObjectiveCType).%N",
                if (regularProperty.primarySirProperty.getter!!.throws) "try " else "",
                regularProperty.primarySirProperty.reference,
            )
        }
    }

    private fun SirProperty.addSetterIfPresent(regularProperty: KotlinRegularPropertySwiftModel) {
        val setter = regularProperty.primarySirProperty.setter ?: return

        SirSetter(
            throws = setter.throws,
            modifiers = listOf(Modifier.NONMUTATING),
        ).addSetterBody(regularProperty, setter)
    }

    private fun SirSetter.addSetterBody(
        regularProperty: KotlinRegularPropertySwiftModel,
        setter: SirSetter,
    ) {
        this.addFunctionBodyWithErrorTypeHandling(regularProperty) {
            addStatement(
                "%L(self as _ObjectiveCType).%N = value",
                if (setter.throws) "try " else "",
                regularProperty.primarySirProperty.reference,
            )
        }
    }
}
