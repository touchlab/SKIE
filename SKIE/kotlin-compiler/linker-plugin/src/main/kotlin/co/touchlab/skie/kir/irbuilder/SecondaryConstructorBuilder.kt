package co.touchlab.skie.kir.irbuilder

import co.touchlab.skie.phases.KotlinIrPhase
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody

class SecondaryConstructorBuilder(val descriptor: ClassConstructorDescriptor) {

    var valueParameters: List<ValueParameterDescriptor> = emptyList()

    var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC

    // TODO Change to context(KotlinIrPhase.Context, DeclarationIrBuilder) once are context implemented properly
    var body: (context(KotlinIrPhase.Context) DeclarationIrBuilder.(IrConstructor) -> IrBody)? = null
}
