package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import kotlin.reflect.KClass

internal interface AnnotationApplicabilityRule<D : DeclarationDescriptor> : ValidationRule<D> {

    val targetAnnotation: KClass<out Annotation>

    override fun isSatisfied(descriptor: D): Boolean =
        !descriptor.hasAnnotation(targetAnnotation) || isAnnotationApplicable(descriptor)

    fun isAnnotationApplicable(descriptor: D): Boolean
}