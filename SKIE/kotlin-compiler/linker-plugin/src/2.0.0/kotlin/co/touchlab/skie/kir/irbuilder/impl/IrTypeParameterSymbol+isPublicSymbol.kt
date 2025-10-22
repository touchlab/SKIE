package co.touchlab.skie.kir.irbuilder.impl

import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterPublicSymbolImpl

val IrTypeParameterSymbol.isPublicSymbol: Boolean
    get() = this is IrTypeParameterPublicSymbolImpl
