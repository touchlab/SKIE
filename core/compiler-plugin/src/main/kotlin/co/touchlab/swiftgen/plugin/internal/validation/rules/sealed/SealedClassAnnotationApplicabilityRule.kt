package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.plugin.internal.util.isSealed
import co.touchlab.swiftgen.plugin.internal.validation.rules.AnnotationApplicabilityRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.ClassBaseValidationRule
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import kotlin.reflect.KClass

internal class SealedClassAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
) : AnnotationApplicabilityRule<ClassDescriptor>, ClassBaseValidationRule {

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to sealed classes / interfaces."

    override fun isAnnotationApplicable(descriptor: ClassDescriptor): Boolean =
        descriptor.isSealed
}
