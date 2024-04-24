package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.LineAndColumn
import org.jetbrains.kotlin.ir.SourceRangeInfo

actual fun createDummyIrFileEntry(name: String): IrFileEntry =
    DummyIrFileEntry(name)

private class DummyIrFileEntry(override val name: String) : IrFileEntry {

    override val maxOffset: Int = 0

    override fun getColumnNumber(offset: Int): Int = 0

    override fun getLineNumber(offset: Int): Int = 0

    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo = SourceRangeInfo(
        name, 0, 0, 0, 0, 0, 0,
    )

    override fun getLineAndColumnNumbers(offset: Int): LineAndColumn =
        LineAndColumn(0, 0)
}
