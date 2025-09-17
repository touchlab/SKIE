package co.touchlab.skie.kir.irbuilder.impl.namespace

import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.LineAndColumn
import org.jetbrains.kotlin.ir.SourceRangeInfo

internal actual class DummyIrFileEntry actual constructor(actual override val name: String) : IrFileEntry {

    actual override val maxOffset: Int = 0

    override val lineStartOffsets: IntArray = intArrayOf()

    override val firstRelevantLineIndex: Int = 0

    actual override fun getColumnNumber(offset: Int): Int = 0

    actual override fun getLineNumber(offset: Int): Int = 0

    actual override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo = SourceRangeInfo(
        name, 0, 0, 0, 0, 0, 0,
    )

    actual override fun getLineAndColumnNumbers(offset: Int): LineAndColumn =
        LineAndColumn(0, 0)
}
