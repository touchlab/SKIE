package co.touchlab.skie.phases

import co.touchlab.skie.context.DescriptorModificationPhaseContext
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

val DescriptorModificationPhase.Context.declarationBuilder: DeclarationBuilder
    get() = typedContext.declarationBuilder

private val DescriptorModificationPhase.Context.typedContext: DescriptorModificationPhaseContext
    get() = context as DescriptorModificationPhaseContext
