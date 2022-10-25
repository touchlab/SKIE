package co.touchlab.skie.plugin.generator.internal.validation.rules.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.plugin.generator.internal.validation.rules.ConflictingAnnotationsRule
import co.touchlab.skie.plugin.generator.internal.validation.rules.ValidationRule
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal object SealedInteropRules {

    val all: List<ValidationRule<ClassDescriptor>> = listOf(
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Hidden::class),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Name::class),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Visible::class),
        ConflictingAnnotationsRule(SealedInterop.Case.Visible::class, SealedInterop.Case.Hidden::class),

        SealedClassAnnotationApplicabilityRule(SealedInterop.Disabled::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.ElseName::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Enabled::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.Name::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.ArgumentLabel::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.ParameterName::class),
        ConflictingAnnotationsRule(SealedInterop.Enabled::class, SealedInterop.Disabled::class),
    )
}
