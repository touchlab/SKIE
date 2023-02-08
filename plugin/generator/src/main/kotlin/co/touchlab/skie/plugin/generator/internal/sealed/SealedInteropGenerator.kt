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
    private val reporter: Reporter,
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
        if (!verifyUniqueCaseNames(swiftModel)) {
            return
        }

        module.generateCode(swiftModel) {
            val classNamespace = addNamespace(swiftGenNamespace, swiftModel.stableFqName)

            val enumType = sealedEnumGeneratorDelegate.generate(swiftModel, classNamespace, this)

            sealedFunctionGeneratorDelegate.generate(swiftModel, enumType, this)
        }
    }

    private fun verifyUniqueCaseNames(swiftModel: KotlinClassSwiftModel): Boolean {
        val conflictingDeclarations = swiftModel.visibleSealedSubclasses
            .groupBy { it.enumCaseName }
            .filter { it.value.size > 1 }
            .values
            .flatten()

        conflictingDeclarations.forEach {
            reportConflictingDeclaration(it)
        }

        return conflictingDeclarations.isEmpty()
    }

    private fun reportConflictingDeclaration(subclass: KotlinClassSwiftModel) {
        val message = "SKIE cannot generate sealed interop for this declaration. " +
            "There are multiple sealed class/interface children with the same name " +
            "`${subclass.enumCaseName}` for the enum case. " +
            "Consider resolving this conflict using configuration `${SealedInterop.Case.Name::class.qualifiedName}` or associated annotation."

        reporter.report(
            severity = Reporter.Severity.Error,
            message = message,
            declaration = subclass.classDescriptor,
        )
    }
}
