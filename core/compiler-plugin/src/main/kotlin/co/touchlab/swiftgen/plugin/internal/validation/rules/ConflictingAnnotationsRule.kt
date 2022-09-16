package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationContainer
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration
import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import kotlin.reflect.KClass

internal class ConflictingAnnotationsRule(
    private val annotations: List<KClass<out Annotation>>,
    override val configuration: Configuration,
) : ValidationRule<ClassDescriptor>, ConfigurationContainer {

    override val message: String =
        "Annotations ${annotations.joinToString { "'${it.qualifiedName}'" }} cannot be used at the same time."

    constructor(configuration: Configuration, vararg annotations: KClass<out Annotation>) : this(annotations.toList(), configuration)

    override fun isSatisfied(descriptor: ClassDescriptor): Boolean =
        annotations.count { descriptor.hasAnnotation(it) } < 2

    override fun severity(descriptor: ClassDescriptor): CompilerMessageSeverity =
        descriptor.getConfiguration(ConfigurationKeys.Validation.Severity).toCompilerMessageSeverity()
}