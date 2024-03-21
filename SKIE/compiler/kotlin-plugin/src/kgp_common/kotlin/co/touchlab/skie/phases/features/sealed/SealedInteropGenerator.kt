package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.MustBeExecutedAfterBridgingConfiguration
import co.touchlab.skie.sir.element.isExported

@MustBeExecutedAfterBridgingConfiguration
class SealedInteropGenerator(
    override val context: SirPhase.Context,
) : SirPhase, SealedGeneratorExtensionContainer {

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(context)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(context)

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allClasses
            .filter { it.isSupported }
            .forEach {
                generate(it)
            }
    }

    private val KirClass.isSupported: Boolean
        get() = this.originalSirClass.isExported &&
            this.isSealed &&
            this.isSealedInteropEnabled

    private val KirClass.isSealedInteropEnabled: Boolean
        get() = configurationProvider.getConfiguration(this, SealedInterop.Enabled)

    context(SirPhase.Context)
    private fun generate(kirClass: KirClass) {
        val enum = sealedEnumGeneratorDelegate.generate(kirClass)

        sealedFunctionGeneratorDelegate.generate(kirClass, enum)
    }
}
