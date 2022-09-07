package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.plugin.internal.validation.rules.AnnotationApplicabilityRule
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isSealed
import kotlin.reflect.KClass

internal class SealedChildrenAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
) : AnnotationApplicabilityRule<ClassDescriptor> {

    override val severity: CompilerMessageSeverity = CompilerMessageSeverity.ERROR

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to direct children of sealed classes / interfaces."

    override fun isAnnotationApplicable(descriptor: ClassDescriptor): Boolean =
        descriptor.typeConstructor.supertypes.any { it.constructor.declarationDescriptor?.isSealed() ?: false }
}
