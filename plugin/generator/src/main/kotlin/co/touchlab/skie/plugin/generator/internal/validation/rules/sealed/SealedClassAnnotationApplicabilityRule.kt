package co.touchlab.skie.plugin.generator.internal.validation.rules.sealed

import co.touchlab.skie.plugin.generator.internal.util.isSealed
import co.touchlab.skie.plugin.generator.internal.validation.rules.AnnotationApplicabilityRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.ClassBaseValidationRule
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
