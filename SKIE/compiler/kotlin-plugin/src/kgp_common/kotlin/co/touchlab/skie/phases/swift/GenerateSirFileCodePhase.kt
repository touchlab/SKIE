package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirVisibility
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
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
            generateDeclarations()

            applyFileBuilderModifications()
        }
    }

    context(SirFile)
    private fun FileSpec.Builder.generateDeclarations() {
        declarations.forEach {
            generateDeclaration(it)
        }
    }

    context(SirFile)
    private fun FileSpec.Builder.applyFileBuilderModifications() {
        swiftPoetBuilderModifications.forEach {
            it()
        }
    }

    private fun FileSpec.Builder.generateDeclaration(declaration: SirDeclaration) {
        when (declaration) {
            is SirTypeAlias -> generateTypeAlias(declaration)
            is SirExtension -> generateExtension(declaration)
            is SirClass -> generateClass(declaration)
            is SirEnumCase,
            -> error("Declaration $declaration cannot be directly inside a file ${declaration.parent}.")
        }
    }

    private fun BuilderWithTypeSpecs.generateTypeAlias(typeAlias: SirTypeAlias) {
        addType(
            TypeAliasSpec.builder(
                name = typeAlias.simpleName,
                type = typeAlias.type.toSwiftPoetUsage(),
            )
                .addVisibility(typeAlias.visibility, typeAlias.defaultVisibility)
                .addTypeParameters(typeAlias)
                .build(),
        )
    }

    private fun <T : BuilderWithModifiers> T.addVisibility(visibility: SirVisibility, defaultVisibility: SirVisibility): T =
        apply {
            if (defaultVisibility == visibility) {
                return@apply
            }

            val modifier = when (visibility) {
                SirVisibility.Public -> Modifier.PUBLIC
                SirVisibility.Internal -> Modifier.INTERNAL
                SirVisibility.Private -> Modifier.PRIVATE
            }

            addModifiers(modifier)
        }

    private fun <T : BuilderWithTypeParameters> T.addTypeParameters(typeDeclaration: SirTypeDeclaration) = apply {
        typeDeclaration.typeParameters.forEach {
            addTypeParameter(it)
        }
    }

    private fun BuilderWithTypeParameters.addTypeParameter(typeDeclaration: SirTypeParameter) {
        val bounds = typeDeclaration.bounds.map { it.toSwiftPoetUsage() }.map { TypeVariableName.Bound(it) }

        addTypeVariable(
            TypeVariableName(typeDeclaration.name).withBounds(bounds),
        )
    }

    private fun FileSpec.Builder.generateExtension(extension: SirExtension) {
        addExtension(
            ExtensionSpec.builder(extension.internalName.toSwiftPoetName())
                .addVisibility(extension.visibility, extension.defaultVisibility)
                .addExtensionDeclarations(extension)
                .applyExtensionBuilderModifications(extension)
                .build(),
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
            is SirExtension,
            is SirEnumCase,
            -> error("Declaration $declaration cannot be directly inside an extension ${declaration.parent}.")
        }
    }

    private fun ExtensionSpec.Builder.applyExtensionBuilderModifications(extension: SirExtension): ExtensionSpec.Builder =
        apply {
            extension.swiftPoetBuilderModifications.forEach {
                it()
            }
        }

    private fun BuilderWithTypeSpecs.generateClass(sirClass: SirClass) {
        addType(
            TypeSpec.Builder(sirClass.swiftPoetKind, sirClass.simpleName)
                .addVisibility(sirClass.visibility, sirClass.defaultVisibility)
                .addSuperTypes(sirClass.superTypes.map { it.toSwiftPoetUsage() })
                .addTypeParameters(sirClass)
                .addClassDeclarations(sirClass)
                .applyClassBuilderModifications(sirClass)
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

    private fun TypeSpec.Builder.addClassDeclaration(declaration: SirDeclaration) {
        when (declaration) {
            is SirTypeAlias -> generateTypeAlias(declaration)
            is SirClass -> generateClass(declaration)
            is SirEnumCase -> generateEnumCase(declaration)
            is SirExtension,
            -> error("Declaration $declaration cannot be directly inside an extension ${declaration.parent}.")
        }
    }

    private fun TypeSpec.Builder.applyClassBuilderModifications(sirClass: SirClass): TypeSpec.Builder =
        apply {
            sirClass.swiftPoetBuilderModifications.forEach {
                it()
            }
        }

    private fun TypeSpec.Builder.generateEnumCase(enumCase: SirEnumCase) {
        val associatedValues = enumCase.associatedValues
            .map { "" to it.type.toSwiftPoetUsage() }
            .takeIf { it.isNotEmpty() }
            ?.let { TupleTypeName.of(it) }

        if (associatedValues != null) {
            addEnumCase(enumCase.simpleName, associatedValues)
        } else {
            addEnumCase(enumCase.simpleName)
        }
    }
}

private val SirDeclaration.defaultVisibility: SirVisibility
    get() = (parent as? SirExtension)?.visibility ?: SirVisibility.Internal
