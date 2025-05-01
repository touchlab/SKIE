package co.touchlab.skie.kir.irbuilder.util

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.irFactory
import co.touchlab.skie.shim.createSimpleFunction
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.Name

context(KotlinIrPhase.Context)
fun IrBuilderWithScope.irSimpleFunction(
    name: Name,
    visibility: DescriptorVisibility,
    returnType: IrType,
    origin: IrDeclarationOrigin,
    symbol: IrSimpleFunctionSymbol = IrSimpleFunctionSymbolImpl(),
    modality: Modality = Modality.FINAL,
    isInline: Boolean = false,
    isExternal: Boolean = false,
    isTailrec: Boolean = false,
    isSuspend: Boolean = false,
    isOperator: Boolean = false,
    isInfix: Boolean = false,
    isExpect: Boolean = false,
    isFakeOverride: Boolean = origin == IrDeclarationOrigin.FAKE_OVERRIDE,
    body: DeclarationIrBuilder.() -> IrBody,
): IrSimpleFunction = irFactory.createSimpleFunction(
    startOffset = startOffset,
    endOffset = endOffset,
    origin = origin,
    symbol = symbol,
    name = name,
    visibility = visibility,
    modality = modality,
    returnType = returnType,
    isInline = isInline,
    isExternal = isExternal,
    isTailrec = isTailrec,
    isSuspend = isSuspend,
    isOperator = isOperator,
    isInfix = isInfix,
    isExpect = isExpect,
    isFakeOverride = isFakeOverride,
).apply {
    val irDeclarationBuilder = DeclarationIrBuilder(context, symbol, startOffset = startOffset, endOffset = endOffset)

    this.body = irDeclarationBuilder.body()

    this.patchDeclarationParents(this@irSimpleFunction.parent)
}
