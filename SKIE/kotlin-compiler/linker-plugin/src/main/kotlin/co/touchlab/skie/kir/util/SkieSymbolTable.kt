package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.SymbolTable

class SkieSymbolTable(
    val kotlinSymbolTable: SymbolTable,
) {

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    // signaturer is optional in Kotlin 2.0.0 but not null in our case
    val signaturer: IdSignatureComposer
        get() = kotlinSymbolTable.signaturer!!

    val descriptorExtension = kotlinSymbolTable.descriptorExtension
}
