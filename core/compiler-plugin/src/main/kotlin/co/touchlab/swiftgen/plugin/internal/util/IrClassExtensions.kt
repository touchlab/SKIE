package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass

@Deprecated("Descriptors")
internal val IrClass.isSealed: Boolean
    get() = this.modality == Modality.SEALED
