package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrBindablePublicSymbolBase
import org.jetbrains.kotlin.ir.util.IdSignature

internal class IrRebindableConstructorPublicSymbol(signature: IdSignature, descriptor: ClassConstructorDescriptor) :
    IrBindablePublicSymbolBase<ClassConstructorDescriptor, IrConstructor>(signature, descriptor), IrConstructorSymbol
