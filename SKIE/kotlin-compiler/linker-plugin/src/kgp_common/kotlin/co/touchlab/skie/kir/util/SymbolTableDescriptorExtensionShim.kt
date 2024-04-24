package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

// WIP Move to shim package
expect class SymbolTableDescriptorExtensionShim(symbolTable: ReferenceSymbolTable) {

    fun referenceFunction(callable: CallableDescriptor): IrFunctionSymbol

    fun referenceProperty(descriptor: PropertyDescriptor): IrPropertySymbol

    fun referenceSimpleFunction(descriptor: FunctionDescriptor): IrSimpleFunctionSymbol

    fun referenceConstructor(descriptor: ClassConstructorDescriptor): IrConstructorSymbol

    fun referenceClass(descriptor: ClassDescriptor): IrClassSymbol
}
