package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.SymbolTable

class SkieSymbolTable(val kotlinSymbolTable: SymbolTable) {

    // signaturer is optional in Kotlin 2.0.0 but not null in our case
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    val signaturer: IdSignatureComposer
        get() = kotlinSymbolTable.signaturer!!

    val descriptorExtension: SymbolTableDescriptorExtensionShim = SymbolTableDescriptorExtensionShim(kotlinSymbolTable)
}
