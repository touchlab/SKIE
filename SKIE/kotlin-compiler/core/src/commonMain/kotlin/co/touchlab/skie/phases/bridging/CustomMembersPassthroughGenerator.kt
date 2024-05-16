package co.touchlab.skie.phases.bridging

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.*
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.joinToCode

object CustomMembersPassthroughGenerator {
    context(SirPhase.Context)
    fun generatePassthroughForDeclarations(
        targetBridge: SirClass,
        declarations: List<CustomPassthroughDeclaration>,
        delegateAccessor: CodeBlock,
    ) {
        declarations.forEach {
            targetBridge.addPassthroughForDeclaration(it, delegateAccessor)
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForDeclaration(declaration: CustomPassthroughDeclaration, delegateAccessor: CodeBlock) {
        when (declaration) {
            is CustomPassthroughDeclaration.SimpleFunction -> addPassthroughForFunction(declaration, delegateAccessor)
            is CustomPassthroughDeclaration.Property -> addPassthroughForProperty(declaration, delegateAccessor)
        }
    }


    context(SirPhase.Context)
    private fun SirClass.addPassthroughForFunction(function: CustomPassthroughDeclaration.SimpleFunction, delegateAccessor: CodeBlock) {
        SirSimpleFunction(
            identifier = function.identifier,
            returnType = function.returnType,
            visibility = function.visibility,
            scope = function.scope,
            isAsync = function.isAsync,
            throws = function.throws,
        ).apply {
            function.valueParameters.forEach { parameter ->
                SirValueParameter(
                    name = parameter.name,
                    label = parameter.label,
                    type = parameter.type,
                )
            }

            addFunctionBody(function, delegateAccessor)
        }
    }

    private fun SirSimpleFunction.addFunctionBody(function: CustomPassthroughDeclaration.SimpleFunction, delegateAccessor: CodeBlock) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(this) {
            addCode(
                function.transformBody(
                    CodeBlock.of(
                        "%L%L%L.%L",
                        if (function.throws) "try " else "",
                        if (function.isAsync) "await " else "",
                        delegateAccessor,
                        function.call(),
                    )
                )
            )
        }
    }

    private fun CustomPassthroughDeclaration.SimpleFunction.call(): CodeBlock {
        val argumentsWithLabels = valueParameters.map { parameter ->
            if (parameter.label == "_") {
                parameter.transformAccess(CodeBlock.of("%L", parameter.name))
            } else {
                CodeBlock.of(
                    "%N: %L",
                    parameter.label ?: parameter.name,
                    parameter.transformAccess(CodeBlock.of("%L", parameter.name)),
                )
            }
        }.joinToCode(", ")

        return CodeBlock.of(
            "%L(%L)",
            identifier,
            argumentsWithLabels,
        )
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughForProperty(property: CustomPassthroughDeclaration.Property, delegateAccessor: CodeBlock) {
        SirProperty(
            identifier = property.identifier,
            type = property.type,
            visibility = property.visibility,
            scope = property.scope,
        ).apply {
            addGetter(property, delegateAccessor)
            addSetter(property, delegateAccessor)
        }
    }

    private fun SirProperty.addGetter(property: CustomPassthroughDeclaration.Property, delegateAccessor: CodeBlock) {
        SirGetter().apply {
            this.addFunctionDeclarationBodyWithErrorTypeHandling(this@addGetter) {
                addCode(
                    property.transformGetter(
                        CodeBlock.of(
                            "%L.%N",
                            delegateAccessor,
                            property.identifier,
                        )
                    )
                )
            }
        }
    }

    private fun SirProperty.addSetter(property: CustomPassthroughDeclaration.Property, delegateAccessor: CodeBlock) {
        val setter = property.setter ?: return
        val parent = parent

        SirSetter(
            modifiers = listOfNotNull(Modifier.NONMUTATING.takeIf { parent is SirClass && parent.kind != SirClass.Kind.Class }),
        ).addSetterBody(property, setter, delegateAccessor)
    }

    private fun SirSetter.addSetterBody(
        property: CustomPassthroughDeclaration.Property,
        setter: CustomPassthroughDeclaration.Property.Setter,
        delegateAccessor: CodeBlock,
    ) {
        this.addFunctionDeclarationBodyWithErrorTypeHandling(this.property) {
            // TODO Remove this once SKIE generates custom header
            when (setter) {
                CustomPassthroughDeclaration.Property.Setter.MutableProperty -> {
                    addStatement(
                        "%L.%N = value",
                        delegateAccessor,
                        property.identifier,
                    )
                }
                is CustomPassthroughDeclaration.Property.Setter.SimpleFunction -> {
                    addStatement(
                        "%L.%N(%L)",
                        delegateAccessor,
                        setter.identifier,
                        setter.parameterLabel?.let {
                            CodeBlock.of("%N: value", it)
                        } ?: CodeBlock.of("value"),
                    )
                }
            }
        }
    }
}
