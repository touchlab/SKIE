package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.plugin.internal.validation.rules.ConflictingAnnotationsRule
import co.touchlab.swiftgen.plugin.internal.validation.rules.ValidationRule
import org.jetbrains.kotlin.ir.declarations.IrClass

internal object SealedInteropRules {

    val all: List<ValidationRule<IrClass>> = listOf(
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Hidden::class),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Name::class),
        SealedChildrenAnnotationApplicabilityRule(SealedInterop.Case.Visible::class),
        ConflictingAnnotationsRule(SealedInterop.Case.Visible::class, SealedInterop.Case.Hidden::class),

        SealedClassAnnotationApplicabilityRule(SealedInterop.Disabled::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.ElseName::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.Enabled::class),
        SealedClassAnnotationApplicabilityRule(SealedInterop.FunctionName::class),
        ConflictingAnnotationsRule(SealedInterop.Enabled::class, SealedInterop.Disabled::class),
    )
}