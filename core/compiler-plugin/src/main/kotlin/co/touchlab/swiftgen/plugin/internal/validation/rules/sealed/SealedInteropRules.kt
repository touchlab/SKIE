package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.validation.rules.ConflictingAnnotationsRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedInteropRules(configuration: Configuration) {

    val all: List<ValidationRule<ClassDescriptor>> = listOf(
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Hidden::class, configuration),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Name::class, configuration),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Visible::class, configuration),
        ConflictingAnnotationsRule(configuration, SealedInterop.Case.Visible::class, SealedInterop.Case.Hidden::class),

        SealedClassAnnotationApplicabilityRule(SealedInterop.Disabled::class, configuration),
        SealedClassAnnotationApplicabilityRule(SealedInterop.ElseName::class, configuration),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Enabled::class, configuration),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.Name::class, configuration),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.ArgumentLabel::class, configuration),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Function.ParameterName::class, configuration),
        ConflictingAnnotationsRule(configuration, SealedInterop.Enabled::class, SealedInterop.Disabled::class),
    )
}