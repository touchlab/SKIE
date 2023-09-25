package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel

class SealedInteropGenerator(
    override val context: SirPhase.Context,
) : SirPhase, SealedGeneratorExtensionContainer {

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(context)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(context)

    context(SirPhase.Context)
    override fun execute() {
        exposedClasses
            .filter { it.isSupported }
            .forEach {
                generate(it)
            }
    }

    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.isSealed && this.isSealedInteropEnabled

    private val KotlinClassSwiftModel.isSealedInteropEnabled: Boolean
        get() = this.getConfiguration(SealedInterop.Enabled)

    context(SirPhase.Context)
    private fun generate(swiftModel: KotlinClassSwiftModel) {
        val enum = sealedEnumGeneratorDelegate.generate(swiftModel)

        sealedFunctionGeneratorDelegate.generate(swiftModel, enum)
    }
}
