package co.touchlab.skie.plugin.generator.internal.validation.rules.sealed

import co.touchlab.skie.plugin.generator.internal.validation.rules.AnnotationApplicabilityRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.ClassBaseValidationRule
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isSealed
import kotlin.reflect.KClass

internal class SealedChildrenAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
) : AnnotationApplicabilityRule<ClassDescriptor>, ClassBaseValidationRule {

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to direct children of sealed classes / interfaces."

    override fun isAnnotationApplicable(descriptor: ClassDescriptor): Boolean =
        descriptor.typeConstructor.supertypes.any { it.constructor.declarationDescriptor?.isSealed() ?: false }
}
