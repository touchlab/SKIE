package co.touchlab.skie.phases.validation.rules

import co.touchlab.skie.kir.util.hasAnnotation
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import kotlin.reflect.KClass

internal class ConflictingAnnotationsRule(
    private val annotations: List<KClass<out Annotation>>,
) : BaseValidationRule<ClassDescriptor>, ClassBaseValidationRule {

    override val message: String =
        "Annotations ${annotations.joinToString { "'${it.qualifiedName}'" }} cannot be used at the same time."

    constructor(vararg annotations: KClass<out Annotation>) : this(annotations.toList())

    override fun isSatisfied(descriptor: ClassDescriptor): Boolean =
        annotations.count { descriptor.hasAnnotation(it) } < 2
}
