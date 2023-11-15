package co.touchlab.skie.phases.features.enums

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
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.joinToCode

object ExhaustiveEnumsMembersPassthroughGenerator {

    private val unsupportedFunctionNames = listOf("compareTo(other:)", "hash()", "description()", "isEqual(_:)")

    private val SirSimpleFunction.isSupported: Boolean
        get() = this.name !in unsupportedFunctionNames

    context(SirPhase.Context)
    fun generatePassthroughForMembers(enumKirClass: KirClass, bridgedEnum: SirClass) {
        enumKirClass.callableDeclarations
            .forEach {
                bridgedEnum.addPassthroughForMember(it)
            }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForMember(member: KirCallableDeclaration<*>) {
        when (member) {
            is KirConstructor -> {
                // Constructors do not need passthrough
            }
            is KirSimpleFunction -> addPassthroughForFunction(member)
            is KirProperty -> addPassthroughForProperty(member)
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForFunction(function: KirSimpleFunction) {
        function.forEachAssociatedExportedSirDeclaration {
            addPassthroughForFunction(it)
        }
    }

    private fun SirClass.addPassthroughForFunction(function: SirSimpleFunction) {
        if (!function.isSupported) {
            return
        }

        function.shallowCopy(parent = this).apply {
            copyValueParametersFrom(function)

            addFunctionBody(function)
        }
    }

    private fun SirSimpleFunction.addFunctionBody(function: SirSimpleFunction) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L(self as _ObjectiveCType).%L",
                if (function.throws) "try " else "",
                if (function.isAsync) "await " else "",
                function.call(function.valueParameters.map { CodeBlock.toString("%N", it.name) }),
            )
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForProperty(property: KirProperty) {
        property.forEachAssociatedExportedSirDeclaration {
            addPassthroughForProperty(it)
        }
    }

    private fun SirClass.addPassthroughForProperty(property: SirProperty) {
        property.shallowCopy(
            parent = this,
        ).apply {
            addGetter(property)
            addSetter(property)
        }
    }

    private fun SirProperty.addGetter(property: SirProperty) {
        val getter = property.getter ?: return

        SirGetter(
            throws = getter.throws,
        ).addGetterBody(property, getter)
    }

    private fun SirGetter.addGetterBody(property: SirProperty, getter: SirGetter) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(property) {
            addStatement(
                "return %L(self as _ObjectiveCType).%N",
                if (getter.throws) "try " else "",
                property.reference,
            )
        }
    }

    private fun SirProperty.addSetter(property: SirProperty) {
        val setter = property.setter ?: return

        SirSetter(
            throws = setter.throws,
            modifiers = listOf(Modifier.NONMUTATING),
        ).addSetterBody(property, setter)
    }

    private fun SirSetter.addSetterBody(
        property: SirProperty,
        setter: SirSetter,
    ) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(property) {
            addStatement(
                "%L(self as _ObjectiveCType).%N = value",
                if (setter.throws) "try " else "",
                property.reference,
            )
        }
    }
}
