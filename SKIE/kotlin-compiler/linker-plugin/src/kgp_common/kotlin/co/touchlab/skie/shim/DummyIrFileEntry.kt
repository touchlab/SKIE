package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.IrFileEntry

expect fun createDummyIrFileEntry(name: String): IrFileEntry
