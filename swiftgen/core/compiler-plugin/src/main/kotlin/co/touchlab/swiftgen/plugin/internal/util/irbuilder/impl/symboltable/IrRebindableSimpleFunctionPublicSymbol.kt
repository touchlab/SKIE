package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

internal class IrRebindableSimpleFunctionPublicSymbol(signature: IdSignature, descriptor: FunctionDescriptor) :
    IrBaseRebindablePublicSymbol<FunctionDescriptor, IrSimpleFunction>(signature, descriptor), IrSimpleFunctionSymbol
