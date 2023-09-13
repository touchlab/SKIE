package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

expect class DummyIrSimpleFunction(symbol: IrSimpleFunctionSymbol) : IrSimpleFunction
