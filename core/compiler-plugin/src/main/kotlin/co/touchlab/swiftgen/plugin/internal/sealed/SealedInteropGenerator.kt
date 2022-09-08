package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import co.touchlab.swiftgen.plugin.internal.util.isSealed
import co.touchlab.swiftgen.plugin.internal.util.isVisibleFromSwift
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedInteropGenerator(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    override val configuration: SwiftGenConfiguration.SealedInteropDefaults,
    override val swiftPackModuleBuilder: SwiftPackModuleBuilder,
    private val reporter: Reporter,
) : BaseGenerator(fileBuilderFactory, namespaceProvider), SealedGeneratorExtensionContainer {

    private val sealedEnumGeneratorDelegate = SealedEnumGeneratorDelegate(configuration, swiftPackModuleBuilder)
    private val sealedFunctionGeneratorDelegate = SealedFunctionGeneratorDelegate(configuration, swiftPackModuleBuilder)

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.classDescriptors.forEach {
            generate(it)
        }
    }

    private fun generate(declaration: ClassDescriptor) {
        if (!shouldGenerateSealedInterop(declaration) || !verifyUniqueCaseNames(declaration)) {
            return
        }

        generateCode(declaration) {
            val classNamespace = addNamespace(swiftGenNamespace, declaration.kotlinName)

            val enumType = sealedEnumGeneratorDelegate.generate(declaration, classNamespace, this)

            sealedFunctionGeneratorDelegate.generate(declaration, enumType, this)
        }
    }

    private fun shouldGenerateSealedInterop(declaration: ClassDescriptor): Boolean =
        declaration.isSealed && declaration.isSealedInteropEnabled

    private val ClassDescriptor.isSealedInteropEnabled: Boolean
        get() = if (configuration.enabled) {
            !this.hasAnnotation<SealedInterop.Disabled>()
        } else {
            this.hasAnnotation<SealedInterop.Enabled>()
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
        val message = "SwiftGen cannot generate sealed interop for this declaration. " +
                "There are multiple sealed class/interface children with the same name " +
                "`${subclass.enumCaseName}` for the enum case. " +
                "Consider resolving this conflict using annotation `${SealedInterop.Case.Name::class.qualifiedName}`."

        reporter.report(
            severity = CompilerMessageSeverity.ERROR,
            message = message,
            declaration = subclass,
        )
    }
}