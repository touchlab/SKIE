package co.touchlab.skie.context

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.header.util.HeaderDeclarationsProvider
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.swiftmodel.DescriptorBridgeProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelProvider

class SirPhaseContext private constructor(
    mainSkieContext: MainSkieContext,
    swiftModelProvider: SwiftModelProvider,
) : SirPhase.Context, SkiePhase.Context by mainSkieContext, MutableSwiftModelScope by swiftModelProvider {

    constructor(mainSkieContext: MainSkieContext) : this(
        mainSkieContext,
        SwiftModelProvider(
            namer = mainSkieContext.namer,
            descriptorProvider = mainSkieContext.descriptorProvider,
            bridgeProvider = DescriptorBridgeProvider(mainSkieContext.namer),
            sirProvider = SirProvider(
                namer = mainSkieContext.namer,
                framework = mainSkieContext.framework,
                descriptorProvider = mainSkieContext.descriptorProvider,
                sdkPath = mainSkieContext.configurables.absoluteTargetSysRoot,
                reporter = mainSkieContext.reporter,
            ),
        ),
    )

    override val headerDeclarationsProvider: HeaderDeclarationsProvider = HeaderDeclarationsProvider(framework.kotlinHeader)

    override val context: SirPhase.Context = this
}
