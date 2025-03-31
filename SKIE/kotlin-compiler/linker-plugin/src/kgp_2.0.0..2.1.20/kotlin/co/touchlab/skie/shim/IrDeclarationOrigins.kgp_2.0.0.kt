package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl

actual val SUSPEND_WRAPPER_CHECKED_EXCEPTIONS: IrDeclarationOriginImpl =
    IrDeclarationOriginImpl("SUSPEND_WRAPPER_CHECKED_EXCEPTIONS", true)
