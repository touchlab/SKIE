package co.touchlab.skie.phases

import co.touchlab.skie.context.LinkPhaseContext
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider

val LinkPhase.Context.descriptorKirProvider: DescriptorKirProvider
    get() = typedContext.descriptorKirProvider

private val LinkPhase.Context.typedContext: LinkPhaseContext
    get() = context as LinkPhaseContext
