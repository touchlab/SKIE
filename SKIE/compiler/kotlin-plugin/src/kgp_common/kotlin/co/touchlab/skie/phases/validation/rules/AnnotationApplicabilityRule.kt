package co.touchlab.skie.phases.validation.rules

import co.touchlab.skie.kir.util.hasAnnotation
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import kotlin.reflect.KClass

internal interface AnnotationApplicabilityRule<D : DeclarationDescriptor> : BaseValidationRule<D> {

    val targetAnnotation: KClass<out Annotation>

    override fun isSatisfied(descriptor: D): Boolean =
        !descriptor.hasAnnotation(targetAnnotation) || isAnnotationApplicable(descriptor)

    fun isAnnotationApplicable(descriptor: D): Boolean
}
