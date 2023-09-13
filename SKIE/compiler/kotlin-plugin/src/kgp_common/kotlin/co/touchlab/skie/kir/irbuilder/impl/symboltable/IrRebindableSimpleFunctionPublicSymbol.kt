package co.touchlab.skie.kir.irbuilder.impl.symboltable

import co.touchlab.skie.kir.irbuilder.impl.symboltable.IrBaseRebindablePublicSymbol
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

class IrRebindableSimpleFunctionPublicSymbol(signature: IdSignature, descriptor: FunctionDescriptor) :
    IrBaseRebindablePublicSymbol<FunctionDescriptor, IrSimpleFunction>(signature, descriptor), IrSimpleFunctionSymbol
