package co.touchlab.skie.kir.irbuilder.impl

import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol

val IrTypeParameterSymbol.isPublicSymbol: Boolean
    get() = signature != null
