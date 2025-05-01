@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.kir.irbuilder.impl

import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol

actual val IrTypeParameterSymbol.isPublicSymbol: Boolean
    get() = signature != null
