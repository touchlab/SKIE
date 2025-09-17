package co.touchlab.skie.kir.irbuilder.impl.namespace

import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.LineAndColumn
import org.jetbrains.kotlin.ir.SourceRangeInfo

internal expect class DummyIrFileEntry(name: String) : IrFileEntry {
    override val name: String
    override val maxOffset: Int
    override fun getColumnNumber(offset: Int): Int
    override fun getLineNumber(offset: Int): Int
    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo
    override fun getLineAndColumnNumbers(offset: Int): LineAndColumn
}
