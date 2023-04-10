package co.touchlab.skie.plugin.generator.internal.analytics.air

import co.touchlab.skie.plugin.analytics.air.element.AirAnonymousInitializer
import co.touchlab.skie.plugin.analytics.air.element.AirClass
import co.touchlab.skie.plugin.analytics.air.element.AirConstant
import co.touchlab.skie.plugin.analytics.air.element.AirConstantArray
import co.touchlab.skie.plugin.analytics.air.element.AirConstantClassReference
import co.touchlab.skie.plugin.analytics.air.element.AirConstantEnumReference
import co.touchlab.skie.plugin.analytics.air.element.AirConstantErased
import co.touchlab.skie.plugin.analytics.air.element.AirConstantObject
import co.touchlab.skie.plugin.analytics.air.element.AirConstantPrimitive
import co.touchlab.skie.plugin.analytics.air.element.AirConstructor
import co.touchlab.skie.plugin.analytics.air.element.AirDeclaration
import co.touchlab.skie.plugin.analytics.air.element.AirEnumEntry
import co.touchlab.skie.plugin.analytics.air.element.AirField
import co.touchlab.skie.plugin.analytics.air.element.AirFile
import co.touchlab.skie.plugin.analytics.air.element.AirFunction
import co.touchlab.skie.plugin.analytics.air.element.AirModality
import co.touchlab.skie.plugin.analytics.air.element.AirModule
import co.touchlab.skie.plugin.analytics.air.element.AirOrigin
import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.air.element.AirProperty
import co.touchlab.skie.plugin.analytics.air.element.AirSimpleFunction
import co.touchlab.skie.plugin.analytics.air.element.AirTypeAlias
import co.touchlab.skie.plugin.analytics.air.element.AirTypeDeclaration
import co.touchlab.skie.plugin.analytics.air.element.AirTypeParameter
import co.touchlab.skie.plugin.analytics.air.element.AirValueParameter
import co.touchlab.skie.plugin.analytics.air.element.AirVisibility
import co.touchlab.skie.plugin.analytics.air.type.AirType
import co.touchlab.skie.plugin.analytics.air.type.AirTypeAbbreviation
import co.touchlab.skie.plugin.analytics.air.type.AirTypeArgument
import co.touchlab.skie.plugin.analytics.air.type.AirTypeVariance
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeAlias
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstantArray
import org.jetbrains.kotlin.ir.expressions.IrConstantObject
import org.jetbrains.kotlin.ir.expressions.IrConstantPrimitive
import org.jetbrains.kotlin.ir.expressions.IrConstantValue
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeAliasSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeAbbreviation
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.types.Variance

@OptIn(ObsoleteDescriptorBasedAPI::class)
class IrToAirTransformer(private val descriptorProvider: DescriptorProvider) {

    fun transformToAir(modules: Collection<IrModuleFragment>): AirProject =
        with(Context()) {
            AirProject(
                modules = modules.map { it.toAir() },
            )
        }

    context(Context)
    private fun IrModuleFragment.toAir(): AirModule =
        AirModule(
            name = AirModule.Name(this.name.asString()),
            files = this.files.map { it.toAir() },
            isExported = this.descriptor in descriptorProvider.exposedModules,
        )

    context(Context)
    private fun IrFile.toAir(): AirFile =
        AirFile(
            packageName = AirFile.PackageName(this.fqName.asString()),
            fileName = AirFile.FileName(this.fileEntry.name.substringAfterLast("/")),
            declarations = this.declarations.map { it.toAir() },
            annotations = this.annotations.map { it.toAirConstant() },
        )

    context(Context)
    private fun IrDeclaration.toAir(): AirDeclaration =
        when (this) {
            is IrAnonymousInitializer -> this.toAir()
            is IrClass -> this.toAir()
            is IrEnumEntry -> error("EnumEntry is not considered as Declaration in Air.")
            is IrField -> this.toAir()
            is IrFunction -> this.toAir()
            is IrProperty -> this.toAir()
            is IrTypeAlias -> this.toAir()
            is IrTypeParameter -> error("IrTypeParameter is not considered as Declaration in Air.")
            is IrValueParameter -> error("IrValueParameter is not considered as Declaration in Air.")
            else -> error("Unsupported IrDeclaration: $this")
        }

    context(Context)
    private fun IrValueParameter.toAir(): AirValueParameter =
        AirValueParameter(
            name = AirValueParameter.Name(this.name.asString()),
            type = (this.varargElementType ?: this.type).toAir(),
            defaultValueKind = this.defaultValue?.let { AirDefaultArgumentClassifier.classifyDefaultArgument(it) },
            containedStatementSize = this.defaultValue?.expression.statementSize,
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
            isVararg = this.isVararg,
            isCrossinline = this.isCrossinline,
            isNoinline = this.isNoinline,
        )

    context(Context)
    private fun IrClass.toAir(): AirClass =
        AirClass(
            symbol = this.airSymbol,
            name = AirClass.Name(this.name.asString()),
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
            isExported = this.descriptor in descriptorProvider.exposedClasses,
            visibility = this.visibility.toAir(),
            declarations = this.declarations.filter { it !is IrEnumEntry && !(it is IrClass && it.isCompanion) }.map { it.toAir() },
            enumEntries = this.declarations.filterIsInstance<IrEnumEntry>().map { it.toAir() },
            companionObject = this.companionObject()?.toAir(),
            typeParameters = this.typeParameters.map { it.toAir() },
            kind = this.kind.toAir(),
            modality = this.modality.toAir(),
            isCompanion = this.isCompanion,
            isInner = this.isInner,
            isData = this.isData,
            isExternal = this.isExternal,
            isValue = this.isValue,
            isExpect = this.isExpect,
            isFun = this.isFun,
            superTypes = this.superTypes.map { it.toAir() },
            sealedSubclasses = this.sealedSubclasses.map { it.airSymbol },
        )

    context(Context)
    private fun IrEnumEntry.toAir(): AirEnumEntry =
        AirEnumEntry(
            symbol = this.airSymbol,
            name = AirEnumEntry.Name(this.name.asString()),
            enumEntryClass = this.correspondingClass?.toAir(),
            origin = this.origin.toAir(),
            annotations = this.annotations.map { it.toAirConstant() },
            containedStatementSize = this.initializerExpression.statementSize,
        )

    context(Context)
    private fun IrTypeParameter.toAir(): AirTypeParameter =
        AirTypeParameter(
            symbol = this.airSymbol,
            name = AirTypeParameter.Name(this.name.asString()),
            isReified = this.isReified,
            variance = this.variance.toAir(),
            superTypes = this.superTypes.map { it.toAir() },
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
        )

    context(Context)
    private fun IrAnonymousInitializer.toAir(): AirAnonymousInitializer =
        AirAnonymousInitializer(
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
            containedStatementSize = this.body.statementSize,
            isStatic = this.isStatic,
        )

    context(Context)
    private fun IrFunction.toAir(): AirFunction =
        when (this) {
            is IrConstructor -> this.toAir()
            is IrSimpleFunction -> this.toAir()
            else -> error("Unsupported IrFunction: $this")
        }

    context(Context)
    private fun IrConstructor.toAir(): AirConstructor =
        AirConstructor(
            symbol = this.airSymbol,
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
            dispatchReceiverParameter = this.dispatchReceiverParameter?.toAir(),
            extensionReceiverParameter = this.extensionReceiverParameter?.toAir(),
            valueParameters = this.valueParameters.map { it.toAir() },
            typeParameters = this.typeParameters.map { it.toAir() },
            returnType = this.returnType.toAir(),
            containedStatementSize = this.body.statementSize,
            isExported = descriptorProvider.isExposed(this.descriptor),
            visibility = this.visibility.toAir(),
            isExternal = this.isExternal,
            isPrimary = this.isPrimary,
            isExpect = this.isExpect,
            contextReceiverParametersCount = this.contextReceiverParametersCount,
        )

    context(Context)
    private fun IrProperty.toAir(): AirProperty =
        AirProperty(
            symbol = this.airSymbol,
            name = AirProperty.Name(this.name.asString()),
            origin = this.origin.toAir(),
            annotations = this.annotations.map { it.toAirConstant() },
            isExported = descriptorProvider.isExposed(this.descriptor),
            visibility = this.visibility.toAir(),
            modality = this.modality.toAir(),
            overriddenSymbols = this.overriddenSymbols.map { it.airSymbol },
            isVar = this.isVar,
            isConst = this.isConst,
            isLateinit = this.isLateinit,
            isDelegated = this.isDelegated,
            isExternal = this.isExternal,
            isExpect = this.isExpect,
            isFakeOverride = this.isFakeOverride,
            backingField = this.backingField?.toAir(),
            getter = this.getter?.toAir(),
            setter = this.setter?.toAir(),
        )

    context(Context)
    private fun IrTypeAlias.toAir(): AirTypeAlias =
        AirTypeAlias(
            symbol = this.airSymbol,
            name = AirTypeAlias.Name(this.name.asString()),
            origin = this.origin.toAir(),
            annotations = this.annotations.map { it.toAirConstant() },
            visibility = this.visibility.toAir(),
            typeParameters = this.typeParameters.map { it.toAir() },
            isActual = this.isActual,
            expandedType = this.expandedType.toAir(),
        )

    context(Context)
    private fun IrField.toAir(): AirField =
        AirField(
            name = AirField.Name(this.name.asString()),
            origin = this.origin.toAir(),
            annotations = this.annotations.map { it.toAirConstant() },
            type = this.type.toAir(),
            visibility = this.visibility.toAir(),
            isFinal = this.isFinal,
            isExternal = this.isExternal,
            isStatic = this.isStatic,
            containedStatementSize = this.initializer.statementSize,
        )

    context(Context)
    private fun IrSimpleFunction.toAir(): AirSimpleFunction =
        AirSimpleFunction(
            symbol = this.airSymbol,
            annotations = this.annotations.map { it.toAirConstant() },
            origin = this.origin.toAir(),
            dispatchReceiverParameter = this.dispatchReceiverParameter?.toAir(),
            extensionReceiverParameter = this.extensionReceiverParameter?.toAir(),
            valueParameters = this.valueParameters.map { it.toAir() },
            typeParameters = this.typeParameters.map { it.toAir() },
            returnType = this.returnType.toAir(),
            containedStatementSize = this.body.statementSize,
            name = AirSimpleFunction.Name(this.name.asString()),
            isExported = descriptorProvider.isExposed(this.descriptor),
            visibility = this.visibility.toAir(),
            modality = this.modality.toAir(),
            isInline = this.isInline,
            isExternal = this.isExternal,
            isTailrec = this.isTailrec,
            isSuspend = this.isSuspend,
            isOperator = this.isOperator,
            isInfix = this.isInfix,
            isExpect = this.isExpect,
            isFakeOverride = this.isFakeOverride,
            overriddenSymbols = this.overriddenSymbols.map { it.airSymbol },
            contextReceiverParametersCount = this.contextReceiverParametersCount,
        )

    context(Context)
    private fun IrConstantValue.toAir(): AirConstant =
        when (this) {
            is IrConstantArray -> this.toAir()
            is IrConstantObject -> this.toAir()
            is IrConstantPrimitive -> this.toAir()
            else -> error("Unsupported IrConstantValue: $this")
        }

    context(Context)
    private fun IrConstantArray.toAir(): AirConstantArray =
        AirConstantArray(
            elements = this.elements.map { it.toAir() },
        )

    context(Context)
    private fun IrConstantObject.toAir(): AirConstantObject =
        AirConstantObject(
            constructor = this.constructor.airSymbol,
            valueArguments = this.valueArguments.map { it.toAir() },
            typeArguments = this.typeArguments.map { it.toAir() },
        )

    context(Context)
    private fun IrConstantPrimitive.toAir(): AirConstantPrimitive =
        this.value.toAir()

    context(Context)
    private fun IrConst<*>.toAir(): AirConstantPrimitive =
        AirConstantPrimitive(
            value = this.value.toString(),
            kind = this.kind.toAir(),
        )

    context(Context)
    private fun IrConstructorCall.toAirConstant(): AirConstantObject =
        AirConstantObject(
            constructor = this.symbol.airSymbol,
            valueArguments = (0 until this.valueArgumentsCount).map { this.getValueArgumentOrDefaultValue(it).toAirConstant() },
            typeArguments = (0 until this.typeArgumentsCount).map { this.getTypeArgument(it)!!.toAir() },
        )

    private fun IrConstructorCall.getValueArgumentOrDefaultValue(index: Int): IrExpression? =
        this.getValueArgument(index) ?: this.symbol.owner.valueParameters[index].defaultValue?.expression

    context(Context)
    private fun IrExpression.toAirConstant(): AirConstant =
        (this as IrExpression?).toAirConstant()

    context(Context)
    @JvmName("optionalIrExpressionToAirConstant")
    private fun IrExpression?.toAirConstant(): AirConstant =
        when (this) {
            is IrConst<*> -> this.toAir()
            is IrConstantValue -> this.toAir()
            is IrConstructorCall -> this.toAirConstant()
            is IrVararg -> this.toAirConstant()
            is IrGetEnumValue -> this.toAirConstant()
            is IrClassReference -> this.toAirConstant()
            is IrErrorExpression, null -> AirConstantErased
            else -> error("Expression is not constant: $this")
        }

    context(Context)
    private fun IrVararg.toAirConstant(): AirConstantArray =
        AirConstantArray(
            elements = this.elements.flatMap { it.toAirConstant() },
        )

    context(Context)
    private fun IrVarargElement.toAirConstant(): List<AirConstant> =
        when (this) {
            is IrExpression -> listOf(this.toAirConstant())
            is IrSpreadElement -> this.toAirConstant()
            else -> error("Unsupported IrVarargElement: $this")
        }

    context(Context)
    private fun IrSpreadElement.toAirConstant(): List<AirConstant> =
        (this.expression.toAirConstant() as AirConstantArray).elements

    context(Context)
    private fun IrGetEnumValue.toAirConstant(): AirConstantEnumReference =
        AirConstantEnumReference(
            symbol = this.symbol.airSymbol,
        )

    context(Context)
    private fun IrClassReference.toAirConstant(): AirConstantClassReference =
        AirConstantClassReference(
            symbol = (this.symbol as IrClassSymbol).airSymbol,
        )

    context(Context)
    private fun IrType.toAir(): AirType {
        check(this is IrSimpleType) { "Unsupported IrType $this." }

        return AirType(
            classifier = this.classifier.airSymbol,
            isNullable = this.isNullable(),
            arguments = this.arguments.map { it.toAir() },
            annotations = this.annotations.map { it.toAirConstant() },
            abbreviation = this.abbreviation?.toAir(),
        )
    }

    context(Context)
    private fun IrTypeArgument.toAir(): AirTypeArgument =
        when (this) {
            is IrStarProjection -> this.toAir()
            is IrTypeProjection -> this.toAir()
            else -> error("Unsupported IrTypeArgument: $this")
        }

    context(Context)
    @Suppress("UnusedReceiverParameter")
    private fun IrStarProjection.toAir(): AirTypeArgument.StarProjection =
        AirTypeArgument.StarProjection

    context(Context)
    private fun IrTypeProjection.toAir(): AirTypeArgument.TypeProjection =
        AirTypeArgument.TypeProjection(
            variance = this.variance.toAir(),
            type = this.type.toAir(),
        )

    context(Context)
    private fun IrTypeAbbreviation.toAir(): AirTypeAbbreviation =
        AirTypeAbbreviation(
            typeAlias = this.typeAlias.airSymbol,
            hasQuestionMark = this.hasQuestionMark,
            arguments = this.arguments.map { it.toAir() },
            annotations = this.annotations.map { it.toAirConstant() },
        )

    private fun IrDeclarationOrigin.toAir(): AirOrigin =
        AirOrigin(
            name = this.toString(),
            isSynthetic = this.isSynthetic,
        )

    private fun ClassKind.toAir(): AirClass.Kind =
        when (this) {
            ClassKind.CLASS -> AirClass.Kind.Class
            ClassKind.INTERFACE -> AirClass.Kind.Interface
            ClassKind.ENUM_CLASS -> AirClass.Kind.Enum
            ClassKind.ENUM_ENTRY -> AirClass.Kind.EnumEntry
            ClassKind.ANNOTATION_CLASS -> AirClass.Kind.Annotation
            ClassKind.OBJECT -> AirClass.Kind.Object
        }

    private fun Modality.toAir(): AirModality =
        when (this) {
            Modality.FINAL -> AirModality.Final
            Modality.SEALED -> AirModality.Sealed
            Modality.OPEN -> AirModality.Open
            Modality.ABSTRACT -> AirModality.Abstract
        }

    private fun DescriptorVisibility.toAir(): AirVisibility =
        when (this.delegate) {
            is Visibilities.Inherited -> AirVisibility.Inherited
            is Visibilities.Internal -> AirVisibility.Internal
            is Visibilities.InvisibleFake -> AirVisibility.InvisibleFake
            is Visibilities.Local -> AirVisibility.Local
            is Visibilities.Private -> AirVisibility.Private
            is Visibilities.PrivateToThis -> AirVisibility.PrivateToThis
            is Visibilities.Protected -> AirVisibility.Protected
            is Visibilities.Public -> AirVisibility.Public
            is Visibilities.Unknown -> AirVisibility.Unknown
            else -> error("Unsupported visibility: ${this.delegate}")
        }

    private fun Variance.toAir(): AirTypeVariance =
        when (this) {
            Variance.INVARIANT -> AirTypeVariance.Invariant
            Variance.IN_VARIANCE -> AirTypeVariance.Contravariant
            Variance.OUT_VARIANCE -> AirTypeVariance.Covariant
        }

    private fun <T> IrConstKind<T>.toAir(): AirConstantPrimitive.Kind =
        when (this) {
            IrConstKind.Boolean -> AirConstantPrimitive.Kind.Boolean
            IrConstKind.Byte -> AirConstantPrimitive.Kind.Byte
            IrConstKind.Char -> AirConstantPrimitive.Kind.Char
            IrConstKind.Double -> AirConstantPrimitive.Kind.Double
            IrConstKind.Float -> AirConstantPrimitive.Kind.Float
            IrConstKind.Int -> AirConstantPrimitive.Kind.Int
            IrConstKind.Long -> AirConstantPrimitive.Kind.Long
            IrConstKind.Null -> AirConstantPrimitive.Kind.Null
            IrConstKind.Short -> AirConstantPrimitive.Kind.Short
            IrConstKind.String -> AirConstantPrimitive.Kind.String
        }

    private val IrStatement?.statementSize: Int
        get() = AirStatementCounter.statementSize(this)

    private val IrBody?.statementSize: Int
        get() = AirStatementCounter.statementSize(this)

    private class Context {

        private var nextIndex = 0

        private val classSymbols = mutableMapOf<IrClass, AirClass.Symbol>()
        private val functionSymbols = mutableMapOf<IrFunction, AirFunction.Symbol>()
        private val propertySymbols = mutableMapOf<IrProperty, AirProperty.Symbol>()
        private val typeParameterSymbols = mutableMapOf<IrTypeParameter, AirTypeParameter.Symbol>()
        private val enumEntrySymbols = mutableMapOf<IrEnumEntry, AirEnumEntry.Symbol>()
        private val typeAliasSymbols = mutableMapOf<IrTypeAlias, AirTypeAlias.Symbol>()

        val IrClass.airSymbol: AirClass.Symbol
            get() = classSymbols.getOrPut(this) {
                AirClass.Symbol(nextIndex++)
            }

        val IrFunction.airSymbol: AirFunction.Symbol
            get() = functionSymbols.getOrPut(this) {
                AirFunction.Symbol(nextIndex++)
            }

        val IrProperty.airSymbol: AirProperty.Symbol
            get() = propertySymbols.getOrPut(this) {
                AirProperty.Symbol(nextIndex++)
            }

        val IrTypeParameter.airSymbol: AirTypeParameter.Symbol
            get() = typeParameterSymbols.getOrPut(this) {
                AirTypeParameter.Symbol(nextIndex++)
            }

        val IrEnumEntry.airSymbol: AirEnumEntry.Symbol
            get() = enumEntrySymbols.getOrPut(this) {
                AirEnumEntry.Symbol(nextIndex++)
            }

        val IrTypeAlias.airSymbol: AirTypeAlias.Symbol
            get() = typeAliasSymbols.getOrPut(this) {
                AirTypeAlias.Symbol(nextIndex++)
            }

        val IrClassSymbol.airSymbol: AirClass.Symbol
            get() = this.owner.airSymbol

        val IrFunctionSymbol.airSymbol: AirFunction.Symbol
            get() = this.owner.airSymbol

        val IrPropertySymbol.airSymbol: AirProperty.Symbol
            get() = this.owner.airSymbol

        val IrTypeParameterSymbol.airSymbol: AirTypeParameter.Symbol
            get() = this.owner.airSymbol

        val IrEnumEntrySymbol.airSymbol: AirEnumEntry.Symbol
            get() = this.owner.airSymbol

        val IrTypeAliasSymbol.airSymbol: AirTypeAlias.Symbol
            get() = this.owner.airSymbol

        val IrClassifierSymbol.airSymbol: AirTypeDeclaration.Symbol
            get() = when (this) {
                is IrClassSymbol -> this.airSymbol
                is IrTypeParameterSymbol -> this.airSymbol
                else -> error("Unsupported IrClassifierSymbol: $this")
            }
    }
}
