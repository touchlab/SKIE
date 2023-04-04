package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider

internal class SealedInteropGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
) : BaseGenerator(skieContext, namespaceProvider), SealedGeneratorExtensionContainer {

    override val isActive: Boolean = true

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(skieContext)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(skieContext)

    override fun runObjcPhase() {
        module.configure {
            exposedClasses
                .filter { it.isSupported }
                .forEach {
                    generate(it)
                }
        }
    }

    private val KotlinClassSwiftModel.isSupported: Boolean
        get() = this.isSealed && this.isSealedInteropEnabled

    private val KotlinClassSwiftModel.isSealedInteropEnabled: Boolean
        get() = this.getConfiguration(SealedInterop.Enabled)

    private fun generate(swiftModel: KotlinClassSwiftModel) {
        module.generateCode(swiftModel) {
            val classNamespace = addNamespaceFor(swiftModel.nonBridgedDeclaration.publicName)

            val enumType = sealedEnumGeneratorDelegate.generate(swiftModel, classNamespace, this)

            sealedFunctionGeneratorDelegate.generate(swiftModel, enumType, this)
        }
    }
}
