package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.ir.util.SymbolTable

class SkieSymbolTable(
    val kotlinSymbolTable: SymbolTable,
) {

    val descriptorExtension: SymbolTableDescriptorExtensionShim = SymbolTableDescriptorExtensionShim(kotlinSymbolTable)
}
