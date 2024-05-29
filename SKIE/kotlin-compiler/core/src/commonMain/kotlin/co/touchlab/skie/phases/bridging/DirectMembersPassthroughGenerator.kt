package co.touchlab.skie.phases.bridging

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.forEachAssociatedExportedSirDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.call
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.isOverriddenFromReadOnlyProperty
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.Modifier

object DirectMembersPassthroughGenerator {
    private val unsupportedFunctionNames = listOf("compareTo(other:)", "hash()", "description()", "isEqual(_:)")

    private val SirSimpleFunction.isSupported: Boolean
        get() = this.name !in unsupportedFunctionNames

    context(SirPhase.Context)
    fun generatePassthroughForMembers(
        targetBridge: SirClass,
        bridgedKirClass: KirClass,
        delegateAccessor: CodeBlock,
    ) {
        bridgedKirClass.callableDeclarations
            .forEach {
                targetBridge.addPassthroughForMember(it, delegateAccessor)
            }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForMember(member: KirCallableDeclaration<*>, delegateAccessor: CodeBlock) {
        when (member) {
            is KirConstructor -> {
                // Constructors do not need passthrough
            }
            is KirSimpleFunction -> addPassthroughForFunction(member, delegateAccessor)
            is KirProperty -> addPassthroughForProperty(member, delegateAccessor)
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForFunction(function: KirSimpleFunction, delegateAccessor: CodeBlock) {
        function.forEachAssociatedExportedSirDeclaration {
            addPassthroughForFunction(it, delegateAccessor)
        }
    }

    private fun SirClass.addPassthroughForFunction(function: SirSimpleFunction, delegateAccessor: CodeBlock) {
        if (!function.isSupported) {
            return
        }

        function.shallowCopy(parent = this, isFakeOverride = false, isAbstract = false).apply {
            copyValueParametersFrom(function)

            addFunctionBody(function, delegateAccessor)
        }
    }

    private fun SirSimpleFunction.addFunctionBody(function: SirSimpleFunction, delegateAccessor: CodeBlock) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L%L.%L",
                if (function.throws) "try " else "",
                if (function.isAsync) "await " else "",
                delegateAccessor,
                function.call(function.valueParameters),
            )
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForProperty(property: KirProperty, delegateAccessor: CodeBlock) {
        property.forEachAssociatedExportedSirDeclaration {
            addPassthroughForProperty(it, delegateAccessor)
        }
    }

    private fun SirClass.addPassthroughForProperty(property: SirProperty, delegateAccessor: CodeBlock) {
        property.shallowCopy(
            parent = this,
            isFakeOverride = false,
            isAbstract = false,
        ).apply {
            addGetter(property, delegateAccessor)
            addSetter(property, delegateAccessor)
        }
    }

    private fun SirProperty.addGetter(property: SirProperty, delegateAccessor: CodeBlock) {
        val getter = property.getter ?: return

        SirGetter(
            throws = getter.throws,
        ).addGetterBody(property, getter, delegateAccessor)
    }

    private fun SirGetter.addGetterBody(property: SirProperty, getter: SirGetter, delegateAccessor: CodeBlock) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(property) {
            addStatement(
                "return %L%L.%N",
                if (getter.throws) "try " else "",
                delegateAccessor,
                property.reference,
            )
        }
    }

    private fun SirProperty.addSetter(property: SirProperty, delegateAccessor: CodeBlock) {
        val setter = property.setter ?: return
        val parent = parent

        SirSetter(
            throws = setter.throws,
            modifiers = listOfNotNull(Modifier.NONMUTATING.takeIf { parent is SirClass && parent.kind != SirClass.Kind.Class }),
        ).addSetterBody(property, setter, delegateAccessor)
    }

    private fun SirSetter.addSetterBody(
        property: SirProperty,
        setter: SirSetter,
        delegateAccessor: CodeBlock,
    ) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(property) {
            // TODO Remove this filter once SKIE generates custom header
            val isParentProtocol = (property.parent as? SirClass)?.kind == SirClass.Kind.Protocol
            if (property.isOverriddenFromReadOnlyProperty && isParentProtocol) {
                addStatement(
                    "%L%L.%N(value)",
                    if (setter.throws) "try " else "",
                    delegateAccessor,
                    "set" + property.reference.replaceFirstChar { it.uppercase() },
                )
            } else {
                addStatement(
                    "%L%L.%N = value",
                    if (setter.throws) "try " else "",
                    delegateAccessor,
                    property.reference,
                )
            }
        }
    }
}
