package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationWithScope
import co.touchlab.skie.sir.element.SirElementWithAttributes
import co.touchlab.skie.sir.element.SirElementWithModifiers
import co.touchlab.skie.sir.element.SirElementWithFunctionBodyBuilder
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirOverridableDeclaration
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirTypeParameterParent
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isRemoved
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.AttributedSpec
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.builder.BuilderWithMembers
import io.outfoxx.swiftpoet.builder.BuilderWithModifiers
import io.outfoxx.swiftpoet.builder.BuilderWithTypeParameters
import io.outfoxx.swiftpoet.builder.BuilderWithTypeSpecs

object GenerateSirFileCodePhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.files.forEach {
            it.generateCode()
        }
    }

    context(SirPhase.Context)
    private fun SirFile.generateCode() {
        val fileBuilder = FileSpec.builder(framework.moduleName, this.name)

        this.generateCodeUsing(fileBuilder)

        val contentOrNull = this.content.takeIf { it.isNotBlank() }
        val generatedCode = fileBuilder.build().toString()

        this.content = listOfNotNull(contentOrNull, generatedCode).joinToString("\n\n")
    }

    private fun SirFile.generateCodeUsing(fileBuilder: FileSpec.Builder) {
        fileBuilder.apply {
            generateImports()

            generateDeclarations()
        }
    }

    context(SirFile)
    private fun FileSpec.Builder.generateImports() {
        imports.forEach {
            addImport(it)
        }
    }

    context(SirFile)
    private fun FileSpec.Builder.generateDeclarations() {
        declarations.forEach {
            generateDeclaration(it)
        }
    }

    private fun FileSpec.Builder.generateDeclaration(declaration: SirDeclaration) {
        when (declaration) {
            is SirTypeAlias -> generateTypeAlias(declaration)
            is SirExtension -> generateExtension(declaration)
            is SirClass -> generateClass(declaration)
            is SirSimpleFunction -> generateFunction(declaration)
            is SirProperty -> generateProperty(declaration)
            is SirConstructor -> error("Declaration $declaration cannot be directly inside a file ${declaration.parent}.")
        }
    }

    private fun BuilderWithTypeSpecs.generateTypeAlias(typeAlias: SirTypeAlias) {
        if (typeAlias.isRemoved) {
            return
        }

        addType(
            TypeAliasSpec.builder(
                name = typeAlias.simpleName,
                type = typeAlias.type.toSwiftPoetTypeName(),
            )
                .addVisibility(typeAlias.visibility, typeAlias.defaultVisibility)
                .addTypeParameters(typeAlias)
                .build(),
        )
    }

    private fun <T : BuilderWithModifiers> T.addVisibility(visibility: SirVisibility, defaultVisibility: SirVisibility): T =
        apply {
            val visibilityModifier = visibility.toSwiftPoetVisibility()
            val defaultVisibilityModifier = defaultVisibility.toSwiftPoetVisibility()

            if (visibilityModifier == defaultVisibilityModifier) {
                return@apply
            }

            addModifiers(visibilityModifier)
        }

    private fun SirVisibility.toSwiftPoetVisibility(): Modifier =
        when (this) {
            SirVisibility.Public,
            SirVisibility.PublicButHidden,
            SirVisibility.PublicButReplaced,
            -> Modifier.PUBLIC
            SirVisibility.Internal -> Modifier.INTERNAL
            SirVisibility.Private -> Modifier.PRIVATE
            SirVisibility.Removed -> error("Removed declarations should not be generated and must be filtered out sooner.")
        }

    private fun <T : BuilderWithTypeParameters> T.addTypeParameters(parent: SirTypeParameterParent) = apply {
        parent.typeParameters.forEach {
            addTypeParameter(it)
        }
    }

    private fun BuilderWithTypeParameters.addTypeParameter(typeDeclaration: SirTypeParameter) {
        val bounds = typeDeclaration.bounds.map { it.toSwiftPoetTypeName() }.map { TypeVariableName.Bound(it) }

        addTypeVariable(
            TypeVariableName.typeVariable(typeDeclaration.name, bounds),
        )
    }

    private fun FileSpec.Builder.generateExtension(extension: SirExtension) {
        if (extension.isRemoved) {
            return
        }

        addExtension(
            ExtensionSpec.builder(extension.classDeclaration.defaultType.toSwiftPoetDeclaredTypeName())
                .addVisibility(extension.visibility, extension.defaultVisibility)
                .addConditionalConstraints(extension)
                .addExtensionDeclarations(extension)
                .build(),
        )
    }

    private fun ExtensionSpec.Builder.addConditionalConstraints(extension: SirExtension): ExtensionSpec.Builder =
        apply {
            extension.conditionalConstraints.forEach {
                addConditionalConstraint(it)
            }
        }

    private fun ExtensionSpec.Builder.addConditionalConstraint(conditionalConstraint: SirConditionalConstraint) {
        val bounds = conditionalConstraint.bounds.map { it.toSwiftPoetTypeName() }.map { TypeVariableName.Bound(it) }

        addConditionalConstraint(
            TypeVariableName.typeVariable(conditionalConstraint.typeParameter.name, bounds),
        )
    }

    private fun ExtensionSpec.Builder.addExtensionDeclarations(extension: SirExtension): ExtensionSpec.Builder =
        apply {
            extension.declarations.forEach {
                addExtensionDeclaration(it)
            }
        }

    private fun ExtensionSpec.Builder.addExtensionDeclaration(declaration: SirDeclaration) {
        when (declaration) {
            is SirTypeAlias -> generateTypeAlias(declaration)
            is SirClass -> generateClass(declaration)
            is SirSimpleFunction -> generateFunction(declaration)
            is SirProperty -> generateProperty(declaration)
            is SirConstructor -> generateConstructor(declaration)
            is SirExtension -> error("Declaration $declaration cannot be directly inside an extension ${declaration.parent}.")
        }
    }

    private fun BuilderWithTypeSpecs.generateClass(sirClass: SirClass) {
        if (sirClass.isRemoved) {
            return
        }

        addType(
            TypeSpec.Builder(sirClass.swiftPoetKind, sirClass.simpleName)
                .addVisibility(sirClass.visibility, sirClass.defaultVisibility)
                .addSuperTypes(sirClass.superTypes.map { it.toSwiftPoetTypeName() })
                .addAttributes(sirClass)
                .addTypeParameters(sirClass)
                .addClassDeclarations(sirClass)
                .addEnumCases(sirClass)
                .build(),
        )
    }

    private val SirClass.swiftPoetKind: TypeSpec.Kind
        get() = when (kind) {
            SirClass.Kind.Class -> TypeSpec.Kind.Class()
            SirClass.Kind.Enum -> TypeSpec.Kind.Enum()
            SirClass.Kind.Struct -> TypeSpec.Kind.Struct()
            SirClass.Kind.Protocol -> TypeSpec.Kind.Protocol()
        }

    private fun TypeSpec.Builder.addClassDeclarations(sirClass: SirClass): TypeSpec.Builder =
        apply {
            sirClass.declarations.forEach {
                addClassDeclaration(it)
            }
        }

    private fun TypeSpec.Builder.addEnumCases(sirClass: SirClass): TypeSpec.Builder =
        apply {
            sirClass.enumCases.forEach {
                generateEnumCase(it)
            }
        }

    private fun TypeSpec.Builder.addClassDeclaration(declaration: SirDeclaration) {
        when (declaration) {
            is SirTypeAlias -> generateTypeAlias(declaration)
            is SirClass -> generateClass(declaration)
            is SirSimpleFunction -> generateFunction(declaration)
            is SirProperty -> generateProperty(declaration)
            is SirConstructor -> generateConstructor(declaration)
            is SirExtension -> error("Declaration $declaration cannot be directly inside an extension ${declaration.parent}.")
        }
    }

    private fun TypeSpec.Builder.generateEnumCase(enumCase: SirEnumCase) {
        val associatedValues = enumCase.associatedValues
            .map { "" to it.type.toSwiftPoetTypeName() }
            .takeIf { it.isNotEmpty() }
            ?.let { TupleTypeName.of(it) }

        if (associatedValues != null) {
            addEnumCase(enumCase.simpleName, associatedValues)
        } else {
            addEnumCase(enumCase.simpleName)
        }
    }

    private fun <T : BuilderWithMembers> T.generateFunction(function: SirSimpleFunction) {
        if (function.isRemoved) {
            return
        }

        addFunction(
            FunctionSpec.builder(function.identifier)
                .addFunctionProperties(function)
                .addOverrideIfNeeded(function)
                .addScope(function)
                .addTypeParameters(function)
                .async(function.isAsync)
                .returns(function.returnType.toSwiftPoetTypeName())
                .build(),
        )
    }

    private fun <T : BuilderWithMembers> T.generateProperty(property: SirProperty) {
        if (property.isRemoved) {
            return
        }

        addProperty(
            PropertySpec.builder(property.identifier, property.type.toSwiftPoetTypeName())
                .addCallableDeclarationProperties(property)
                .addOverrideIfNeeded(property)
                .addScope(property)
                .addGetter(property)
                .addSetter(property)
                .build(),
        )
    }

    private fun PropertySpec.Builder.addGetter(property: SirProperty): PropertySpec.Builder =
        this.apply {
            val getter = property.getter ?: return@apply

            getter(
                FunctionSpec.getterBuilder()
                    .throws(getter.throws)
                    .addAttributes(getter)
                    .applyBuilderModifications(getter)
                    .build(),
            )
        }

    private fun PropertySpec.Builder.addSetter(property: SirProperty): PropertySpec.Builder =
        this.apply {
            val setter = property.setter ?: return@apply

            setter(
                FunctionSpec.setterBuilder()
                    .addParameter(setter.parameterName, property.type.toSwiftPoetTypeName())
                    .throws(setter.throws)
                    .addAttributes(setter)
                    .addModifiers(setter)
                    .applyBuilderModifications(setter)
                    .build(),
            )
        }

    private fun <T : BuilderWithMembers> T.generateConstructor(constructor: SirConstructor) {
        if (constructor.isRemoved) {
            return
        }

        addFunction(
            FunctionSpec.constructorBuilder()
                .addFunctionProperties(constructor)
                .applyIf(constructor.isConvenience) { addModifiers(Modifier.CONVENIENCE) }
                .build(),
        )
    }

    private fun FunctionSpec.Builder.addFunctionProperties(function: SirFunction): FunctionSpec.Builder =
        this.apply {
            addCallableDeclarationProperties(function)
            throws(function.throws)
            addValueParameters(function)
            applyBuilderModifications(function)
        }

    private fun <T> T.addCallableDeclarationProperties(callableDeclaration: SirCallableDeclaration): T
        where T : BuilderWithModifiers, T : AttributedSpec.Builder<*> =
        this.apply {
            addVisibility(callableDeclaration.visibility, callableDeclaration.defaultVisibility)
            addAttributes(callableDeclaration)
            addModifiers(callableDeclaration)
        }

    private fun FunctionSpec.Builder.applyBuilderModifications(
        elementWithSwiftPoetBuilderModifications: SirElementWithFunctionBodyBuilder,
    ): FunctionSpec.Builder =
        apply {
            elementWithSwiftPoetBuilderModifications.bodyBuilder.forEach {
                it(this)
            }
        }

    private fun <T : AttributedSpec.Builder<*>> T.addAttributes(elementWithAttributes: SirElementWithAttributes): T =
        this.apply {
            elementWithAttributes.attributes.forEach {
                addRawAttribute(it)
            }
        }

    private fun <T : AttributedSpec.Builder<*>> T.addRawAttribute(attribute: String) {
        addAttribute(
            AttributeSpec.rawBuilder(attribute).build(),
        )
    }

    private fun <T : BuilderWithModifiers> T.addModifiers(elementWithModifiers: SirElementWithModifiers): T =
        this.apply {
            addModifiers(*elementWithModifiers.modifiers.toTypedArray())
        }

    private fun FunctionSpec.Builder.addValueParameters(function: SirFunction): FunctionSpec.Builder =
        this.apply {
            function.valueParameters.forEach {
                addValueParameter(it)
            }
        }

    private fun FunctionSpec.Builder.addValueParameter(valueParameter: SirValueParameter) {
        val builder = ParameterSpec.builder(valueParameter.label, valueParameter.name, valueParameter.type.toSwiftPoetTypeName())

        if (valueParameter.inout) {
            builder.addModifiers(Modifier.INOUT)
        }

        addParameter(builder.build())
    }

    private fun <T : BuilderWithModifiers> T.addScope(declarationWithScope: SirDeclarationWithScope): T =
        this.apply {
            when (declarationWithScope.scope) {
                SirScope.Static -> addModifiers(Modifier.STATIC)
                SirScope.Class -> addModifiers(Modifier.CLASS)
                SirScope.Member, SirScope.Global -> {
                }
            }
        }

    private fun <T : BuilderWithModifiers> T.addOverrideIfNeeded(overridableDeclaration: SirOverridableDeclaration<*>): T =
        applyIf(overridableDeclaration.needsOverride) {
            addModifiers(Modifier.OVERRIDE)
        }
}

private fun SirType.toSwiftPoetTypeName(): TypeName =
    evaluate().swiftPoetTypeName

private val SirDeclaration.defaultVisibility: SirVisibility
    get() = (parent as? SirExtension)?.visibility ?: SirVisibility.Internal

private val SirOverridableDeclaration<*>.needsOverride: Boolean
    get() = overriddenDeclarations.any { it.memberOwner?.kind == SirClass.Kind.Class }

private fun <T> T.applyIf(condition: Boolean, action: T.() -> Unit): T =
    this.apply {
        if (condition) {
            action()
        }
    }
