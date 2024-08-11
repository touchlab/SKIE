package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.swift.SirCodeGenerator.addSuperTypes
import co.touchlab.skie.phases.swift.SirCodeGenerator.addTypeParameters
import co.touchlab.skie.phases.swift.SirCodeGenerator.addTypeParametersOrAssociatedTypes
import co.touchlab.skie.phases.swift.SirCodeGenerator.toSwiftPoetTypeName
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConditionalConstraintParent
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationWithScope
import co.touchlab.skie.sir.element.SirDeclarationWithSuperTypes
import co.touchlab.skie.sir.element.SirDeclarationWithVisibility
import co.touchlab.skie.sir.element.SirElementWithAttributes
import co.touchlab.skie.sir.element.SirElementWithFunctionBodyBuilder
import co.touchlab.skie.sir.element.SirElementWithModality
import co.touchlab.skie.sir.element.SirElementWithModifiers
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirModality
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
import co.touchlab.skie.util.GeneratedBySkieComment
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.AttributedSpec
import io.outfoxx.swiftpoet.CodeBlock
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
import io.outfoxx.swiftpoet.builder.BuilderWithAssociatedTypes
import io.outfoxx.swiftpoet.builder.BuilderWithConditionalConstraints
import io.outfoxx.swiftpoet.builder.BuilderWithDocs
import io.outfoxx.swiftpoet.builder.BuilderWithMembers
import io.outfoxx.swiftpoet.builder.BuilderWithModifiers
import io.outfoxx.swiftpoet.builder.BuilderWithSuperTypes
import io.outfoxx.swiftpoet.builder.BuilderWithTypeParameters
import io.outfoxx.swiftpoet.builder.BuilderWithTypeSpecs

object SirCodeGenerator {

    fun generate(sirIrFile: SirIrFile): String {
        val fileBuilder = FileSpec.builder(sirIrFile.module.name, sirIrFile.fileNameWithoutSuffix)

        with(sirIrFile) {
            fileBuilder.generateCode()
        }

        return fileBuilder.build().toString()
    }

    context(SirIrFile)
    private fun FileSpec.Builder.generateCode() {
        generateGeneratedComment()

        generateImports()

        generateDeclarations()
    }

    private fun FileSpec.Builder.generateGeneratedComment() {
        addComment(GeneratedBySkieComment)
    }

    context(SirIrFile)
    private fun FileSpec.Builder.generateImports() {
        imports.forEach {
            addImport(it)
        }
    }

    context(SirIrFile)
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
                .addDoc(typeAlias)
                .addVisibility(typeAlias)
                .addTypeParameters(typeAlias)
                .build(),
        )
    }

    private fun <T: BuilderWithDocs<T>, E> T.addDoc(
        element: E,
    ): T where E: SirDeclaration = apply {
        if (element.documentation.isNotBlank()) {
            addDoc(
                CodeBlock.of("%L", element.documentation)
            )
        }
    }

    private fun <T : BuilderWithModifiers, E> T.addVisibilityAndModality(
        element: E,
    ): T where E : SirElementWithModality, E : SirDeclarationWithVisibility =
        apply {
            if (!element.shouldHaveOpenModifier()) {
                addVisibility(element)
            }
            addModifiers(*element.toSwiftPoetModality().toTypedArray())
        }

    private fun <T : BuilderWithModifiers> T.addVisibility(
        declaration: SirDeclarationWithVisibility,
    ): T =
        apply {
            val visibilityModifier = declaration.visibility.toSwiftPoetVisibility()
            val defaultVisibilityModifier = SirVisibility.Internal.toSwiftPoetVisibility()

            if (visibilityModifier == defaultVisibilityModifier) {
                return@apply
            }

            addModifiers(visibilityModifier)
        }

    private fun SirVisibility.toSwiftPoetVisibility(): Modifier =
        when (this) {
            SirVisibility.Public -> Modifier.PUBLIC
            SirVisibility.Internal -> Modifier.INTERNAL
            SirVisibility.Private -> Modifier.PRIVATE
            SirVisibility.Removed -> error("Removed declarations should not be generated and must be filtered out sooner.")
        }

    private fun <E> E.shouldHaveOpenModifier(): Boolean where E : SirElementWithModality, E : SirDeclarationWithVisibility =
        modality == SirModality.Open && visibility == SirVisibility.Public && (parent as? SirClass)?.modality == SirModality.Open

    private fun SirElementWithModality.shouldHaveFinalModifier(): Boolean =
        when (this) {
            is SirClass -> kind == SirClass.Kind.Class && modality == SirModality.Final
            is SirSimpleFunction -> {
                val parent = parent
                modality == SirModality.Final && parent is SirClass && parent.modality != SirModality.Final && scope != SirScope.Static
            }
            is SirProperty -> false
        }

    private fun <E> E.toSwiftPoetModality(): Set<Modifier> where E : SirElementWithModality, E : SirDeclarationWithVisibility =
        setOfNotNull(
            Modifier.FINAL.takeIf { shouldHaveFinalModifier() },
            Modifier.OPEN.takeIf { shouldHaveOpenModifier() },
        )

    private fun <B: BuilderWithAssociatedTypes<B>> B.addTypeParametersOrAssociatedTypes(parent: SirClass) = apply {
        if (parent.kind == SirClass.Kind.Protocol) {
            parent.typeParameters.forEach {
                addAssociatedType(it)
            }
        } else {
            this.addTypeParameters(parent)
        }
    }

    private fun <B: BuilderWithAssociatedTypes<B>> B.addAssociatedType(typeDeclaration: SirTypeParameter) {
        val bounds = typeDeclaration.bounds.map { it.toSwiftPoetTypeVariableNameBound() }

        addAssociatedType(
            TypeVariableName.typeVariable(typeDeclaration.name, bounds)
        )

        if (typeDeclaration.isPrimaryAssociatedType) {
            addTypeVariable(
                TypeVariableName.typeVariable(typeDeclaration.name)
            )
        }
    }

    private fun <T : BuilderWithTypeParameters> T.addTypeParameters(parent: SirTypeParameterParent) = apply {
        parent.typeParameters.forEach {
            addTypeParameter(it)
        }
    }

    private fun BuilderWithTypeParameters.addTypeParameter(typeDeclaration: SirTypeParameter) {
        val bounds = typeDeclaration.bounds.map { it.toSwiftPoetTypeVariableNameBound() }

        addTypeVariable(
            TypeVariableName.typeVariable(typeDeclaration.name, bounds),
        )
    }

    private fun SirTypeParameter.Bound.toSwiftPoetTypeVariableNameBound(): TypeVariableName.Bound {
        val constraint = when (this) {
            is SirTypeParameter.Bound.Conformance -> TypeVariableName.Bound.Constraint.CONFORMS_TO
            is SirTypeParameter.Bound.Equality -> TypeVariableName.Bound.Constraint.SAME_TYPE
        }
        return TypeVariableName.bound(constraint, type.toSwiftPoetTypeName())
    }

    private fun FileSpec.Builder.generateExtension(extension: SirExtension) {
        addExtension(
            ExtensionSpec.builder(extension.classDeclaration.internalName.toSwiftPoetDeclaredTypeName())
                .addDoc(extension)
                .addAttributes(extension)
                .addConditionalConstraints(extension)
                .addExtensionDeclarations(extension)
                .addSuperTypes(extension)
                .build(),
        )
    }

    private fun <B: BuilderWithConditionalConstraints<B>> B.addConditionalConstraints(parent: SirConditionalConstraintParent): B =
        apply {
            parent.conditionalConstraints.forEach {
                addConditionalConstraint(it)
            }
        }

    private fun <B: BuilderWithConditionalConstraints<B>> B.addConditionalConstraint(conditionalConstraint: SirConditionalConstraint) {
        val bounds = conditionalConstraint.bounds.map { it.toSwiftPoetTypeVariableNameBound() }

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
                .addDoc(sirClass)
                .addVisibilityAndModality(sirClass)
                .addSuperTypes(sirClass)
                .addAttributes(sirClass)
                .addTypeParametersOrAssociatedTypes(sirClass)
                .addClassDeclarations(sirClass)
                .addEnumCases(sirClass)
                .addDeinit(sirClass)
                .addVisibilityAndModality(sirClass)
                .build(),
        )
    }

    private fun <B: BuilderWithSuperTypes<B>> B.addSuperTypes(sirDeclarationWithSuperTypes: SirDeclarationWithSuperTypes) =
        apply {
            addSuperTypes(sirDeclarationWithSuperTypes.superTypes.map { it.toSwiftPoetTypeName() })
        }

    private val SirClass.swiftPoetKind: TypeSpec.Kind
        get() = when (kind) {
            SirClass.Kind.Class -> TypeSpec.Kind.Class()
            SirClass.Kind.Enum -> TypeSpec.Kind.Enum()
            SirClass.Kind.Struct -> TypeSpec.Kind.Struct()
            SirClass.Kind.Protocol -> TypeSpec.Kind.Protocol()
            SirClass.Kind.Actor -> TypeSpec.Kind.Actor()
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

    private fun TypeSpec.Builder.addDeinit(sirClass: SirClass): TypeSpec.Builder =
        apply {
            if (sirClass.deinitBuilder.isEmpty()) {
                return@apply
            }

            addFunction(
                FunctionSpec.deinitializerBuilder()
                    .apply {
                        sirClass.deinitBuilder.forEach {
                            it()
                        }
                    }
                    .build(),
            )
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
            FunctionSpec.builder(function.identifierAfterVisibilityChange)
                .addDoc(function)
                .addFunctionProperties(function)
                .addOverrideIfNeeded(function)
                .addScope(function)
                .addTypeParameters(function)
                .addConditionalConstraints(function)
                .addVisibilityAndModality(function)
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
            PropertySpec.builder(property.identifierAfterVisibilityChange, property.type.toSwiftPoetTypeName())
                .addDoc(property)
                .addCallableDeclarationProperties(property)
                .addOverrideIfNeeded(property)
                .addScope(property)
                .addGetter(property)
                .addSetter(property)
                .addVisibilityAndModality(property)
                .mutable(property.isMutable)
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
                .addDoc(constructor)
                .addFunctionProperties(constructor)
                .applyIf(constructor.isConvenience) { addModifiers(Modifier.CONVENIENCE) }
                .addTypeParameters(constructor)
                .addConditionalConstraints(constructor)
                .addVisibility(constructor)
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
            addAttributes(callableDeclaration)
            addModifiers(callableDeclaration)
        }

    private fun FunctionSpec.Builder.applyBuilderModifications(
        elementWithSwiftPoetBuilderModifications: SirElementWithFunctionBodyBuilder,
    ): FunctionSpec.Builder =
        apply {
            if (elementWithSwiftPoetBuilderModifications.bodyBuilder.isNotEmpty()) {
                elementWithSwiftPoetBuilderModifications.bodyBuilder.forEach {
                    it(this)
                }
            } else {
                abstract = true
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
            .addAttributes(valueParameter)

        if (valueParameter.inout) {
            builder.addModifiers(Modifier.INOUT)
        }
        valueParameter.defaultValue?.let {
            builder.defaultValue(it)
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

    private fun SirType.toSwiftPoetTypeName(): TypeName =
        evaluate().swiftPoetTypeName

    private fun <T : BuilderWithModifiers> T.addOverrideIfNeeded(overridableDeclaration: SirOverridableDeclaration<*>): T =
        applyIf(overridableDeclaration.needsOverride) {
            addModifiers(Modifier.OVERRIDE)
        }

    private val SirOverridableDeclaration<*>.needsOverride: Boolean
        get() = overriddenDeclarations.any { it.memberOwner?.kind == SirClass.Kind.Class }

    private fun <T> T.applyIf(condition: Boolean, action: T.() -> Unit): T =
        this.apply {
            if (condition) {
                action()
            }
        }
}
