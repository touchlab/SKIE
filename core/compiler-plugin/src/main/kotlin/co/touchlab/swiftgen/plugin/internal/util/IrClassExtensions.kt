package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass

internal val IrClass.isSealed: Boolean
    get() = this.modality == Modality.SEALED

internal val IrClass.isVisibleFromSwift: Boolean
    get() = this.visibility.isPublicAPI