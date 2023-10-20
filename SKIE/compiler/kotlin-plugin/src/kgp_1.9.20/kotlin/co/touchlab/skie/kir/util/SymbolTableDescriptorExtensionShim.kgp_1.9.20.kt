package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.referenceFunction

@OptIn(ObsoleteDescriptorBasedAPI::class)
actual class SymbolTableDescriptorExtensionShim actual constructor(
    private val symbolTable: ReferenceSymbolTable
) {

    actual fun referenceFunction(callable: CallableDescriptor): IrFunctionSymbol =
        symbolTable.referenceFunction(callable)

    actual fun referenceProperty(descriptor: PropertyDescriptor): IrPropertySymbol =
        symbolTable.descriptorExtension.referenceProperty(descriptor)

    actual fun referenceSimpleFunction(descriptor: FunctionDescriptor): IrSimpleFunctionSymbol =
        symbolTable.descriptorExtension.referenceSimpleFunction(descriptor)

    actual fun referenceConstructor(descriptor: ClassConstructorDescriptor): IrConstructorSymbol =
        symbolTable.descriptorExtension.referenceConstructor(descriptor)

    actual fun referenceClass(descriptor: ClassDescriptor): IrClassSymbol =
        symbolTable.descriptorExtension.referenceClass(descriptor)
}
