package co.touchlab.skie.kir.irbuilder.impl.symboltable

import co.touchlab.skie.kir.irbuilder.impl.symboltable.IrBaseRebindablePublicSymbol
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

class IrRebindableConstructorPublicSymbol(signature: IdSignature, descriptor: ClassConstructorDescriptor) :
    IrBaseRebindablePublicSymbol<ClassConstructorDescriptor, IrConstructor>(signature, descriptor), IrConstructorSymbol
