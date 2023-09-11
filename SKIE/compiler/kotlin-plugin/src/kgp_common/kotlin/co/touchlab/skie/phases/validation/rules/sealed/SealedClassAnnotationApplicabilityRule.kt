package co.touchlab.skie.phases.validation.rules.sealed

import co.touchlab.skie.phases.validation.rules.AnnotationApplicabilityRule
import co.touchlab.skie.phases.validation.rules.ClassBaseValidationRule
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import kotlin.reflect.KClass

internal class SealedClassAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
) : AnnotationApplicabilityRule<ClassDescriptor>, ClassBaseValidationRule {

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to sealed classes / interfaces."

    override fun isAnnotationApplicable(descriptor: ClassDescriptor): Boolean =
        descriptor.modality == Modality.SEALED
}
