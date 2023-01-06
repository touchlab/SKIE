package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.isSealed
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedInteropGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    private val reporter: Reporter,
) : BaseGenerator(skieContext, namespaceProvider, configuration), SealedGeneratorExtensionContainer {

    override val isActive: Boolean = true

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(configuration)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(configuration)

    override fun execute(descriptorProvider: NativeDescriptorProvider) {
        descriptorProvider.exportedClassDescriptors
            .filter { it.shouldHaveSealedInterop }
            .forEach {
                generate(it)
            }
    }

    private val ClassDescriptor.shouldHaveSealedInterop: Boolean
        get() = this.isSealed && this.isSealedInteropEnabled && !this.belongsToSkieRuntime

    private val ClassDescriptor.isSealedInteropEnabled: Boolean
        get() = this.getConfiguration(SealedInterop.Enabled)

    private fun generate(declaration: ClassDescriptor) {
        if (!verifyUniqueCaseNames(declaration)) {
            return
        }

        module.generateCode(declaration) {
            val classNamespace = addNamespace(swiftGenNamespace, declaration.kotlinName)

            val enumType = sealedEnumGeneratorDelegate.generate(declaration, classNamespace, this)

            sealedFunctionGeneratorDelegate.generate(declaration, enumType, this)
        }
    }

    private fun verifyUniqueCaseNames(declaration: ClassDescriptor): Boolean {
        val conflictingDeclarations = declaration.visibleSealedSubclasses
            .groupBy { it.enumCaseName }
            .filter { it.value.size > 1 }
            .values
            .flatten()

        conflictingDeclarations.forEach {
            reportConflictingDeclaration(it)
        }

        return conflictingDeclarations.isEmpty()
    }

    private fun reportConflictingDeclaration(subclass: ClassDescriptor) {
        val message = "SKIE cannot generate sealed interop for this declaration. " +
            "There are multiple sealed class/interface children with the same name " +
            "`${subclass.enumCaseName}` for the enum case. " +
            "Consider resolving this conflict using configuration `${SealedInterop.Case.Name::class.qualifiedName}` or associated annotation."

        reporter.report(
            severity = Reporter.Severity.Error,
            message = message,
            declaration = subclass,
        )
    }
}
