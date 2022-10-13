package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

interface IrReferenceContext {
    fun IrClass.reference(): KotlinClassReference

    fun IrProperty.reference(): KotlinPropertyReference

    fun IrFunction.reference(): KotlinFunctionReference

    fun IrEnumEntry.reference(): KotlinEnumEntryReference
}
