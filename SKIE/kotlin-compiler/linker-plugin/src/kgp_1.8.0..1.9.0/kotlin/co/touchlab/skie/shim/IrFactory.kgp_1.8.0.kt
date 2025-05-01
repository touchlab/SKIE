package co.touchlab.skie.shim

import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

actual fun IrFactory.createSimpleFunction(
    startOffset: Int,
    endOffset: Int,
    origin: IrDeclarationOrigin,
    name: Name,
    visibility: DescriptorVisibility,
    isInline: Boolean,
    isExpect: Boolean,
    returnType: IrType,
    modality: Modality,
    symbol: IrSimpleFunctionSymbol,
    isTailrec: Boolean,
    isSuspend: Boolean,
    isOperator: Boolean,
    isInfix: Boolean,
    isExternal: Boolean,
    containerSource: DeserializedContainerSource?,
    isFakeOverride: Boolean,
): IrSimpleFunction = createFunction(
    startOffset = startOffset,
    endOffset = endOffset,
    origin = origin,
    name = name,
    visibility = visibility,
    isInline = isInline,
    isExpect = isExpect,
    returnType = returnType,
    modality = modality,
    symbol = symbol,
    isTailrec = isTailrec,
    isSuspend = isSuspend,
    isOperator = isOperator,
    isInfix = isInfix,
    isExternal = isExternal,
    containerSource = containerSource,
    isFakeOverride = isFakeOverride,
)
