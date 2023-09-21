package co.touchlab.skie.phases

import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.analytics.AnalyticsCollector

interface DescriptorModificationPhase : SkiePhase<DescriptorModificationPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        override val descriptorProvider: MutableDescriptorProvider

        val declarationBuilder: DeclarationBuilder
    }
}
