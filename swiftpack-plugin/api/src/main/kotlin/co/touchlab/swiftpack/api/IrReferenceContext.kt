package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

interface IrReferenceContext {
    fun IrClass.reference(): KotlinClass

    fun IrProperty.reference(): KotlinProperty

    fun IrFunction.reference(): KotlinFunction

    fun IrEnumEntry.reference(): KotlinEnumEntry
}
