package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationContainer
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration
import co.touchlab.swiftgen.plugin.internal.util.isSealed
import co.touchlab.swiftgen.plugin.internal.validation.rules.AnnotationApplicabilityRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.toCompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import kotlin.reflect.KClass

internal class SealedClassAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
    override val configuration: Configuration,
) : AnnotationApplicabilityRule<ClassDescriptor>, ConfigurationContainer {

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to sealed classes / interfaces."

    override fun isAnnotationApplicable(descriptor: ClassDescriptor): Boolean =
        descriptor.isSealed

    override fun severity(descriptor: ClassDescriptor): CompilerMessageSeverity =
        descriptor.getConfiguration(ConfigurationKeys.Validation.Severity).toCompilerMessageSeverity()
}
