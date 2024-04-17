package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider

interface LinkCompilerPhase :
    LinkPhase<LinkCompilerPhase.Context>,
    ForegroundCompilerPhase<LinkCompilerPhase.Context> {

    interface Context : LinkPhase.Context, ForegroundCompilerPhase.Context {

        override val context: Context

        val descriptorKirProvider: DescriptorKirProvider
    }
}
