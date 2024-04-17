package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider

interface CompilerDependentLinkPhase :
    LinkPhase<CompilerDependentLinkPhase.Context>,
    CompilerDependentForegroundPhase<CompilerDependentLinkPhase.Context> {

    interface Context : LinkPhase.Context, CompilerDependentForegroundPhase.Context {

        override val context: Context

        val descriptorKirProvider: DescriptorKirProvider
    }
}
