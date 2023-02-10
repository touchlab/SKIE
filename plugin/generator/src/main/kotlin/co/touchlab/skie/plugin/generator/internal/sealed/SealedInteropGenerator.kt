package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter

internal class SealedInteropGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
) : BaseGenerator(skieContext, namespaceProvider, configuration), SealedGeneratorExtensionContainer {

    override val isActive: Boolean = true

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(configuration)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(configuration)

    override fun execute() {
        module.configure {
            exposedClasses
                .filter { it.isSupported }
                .forEach {
                    generate(it)
                }
        }
    }

    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.isSealed && this.isSealedInteropEnabled && !this.belongsToSkieRuntime

    private val KotlinClassSwiftModel.isSealedInteropEnabled: Boolean
        get() = this.getConfiguration(SealedInterop.Enabled)

    private fun generate(swiftModel: KotlinClassSwiftModel) {
        module.generateCode(swiftModel) {
            val classNamespace = addNamespace(swiftGenNamespace, swiftModel.stableFqName)

            val enumType = sealedEnumGeneratorDelegate.generate(swiftModel, classNamespace, this)

            sealedFunctionGeneratorDelegate.generate(swiftModel, enumType, this)
        }
    }
}
