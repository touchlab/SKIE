package co.touchlab.swiftgen.plugin.internal.validation.rules.sealed

import co.touchlab.swiftgen.plugin.internal.util.isSealed
import co.touchlab.swiftgen.plugin.internal.validation.rules.AnnotationApplicabilityRule
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.classOrNull
import kotlin.reflect.KClass

internal class SealedChildrenAnnotationApplicabilityRule(
    override val targetAnnotation: KClass<out Annotation>,
) : AnnotationApplicabilityRule<IrClass> {

    override val severity: CompilerMessageSeverity = CompilerMessageSeverity.ERROR

    override val message: String =
        "Annotation '${targetAnnotation.qualifiedName}' can be applied only to direct children of sealed classes / interfaces."

    override fun isAnnotationApplicable(element: IrClass): Boolean =
        element.superTypes.any { it.classOrNull?.owner?.isSealed ?: false }
}
