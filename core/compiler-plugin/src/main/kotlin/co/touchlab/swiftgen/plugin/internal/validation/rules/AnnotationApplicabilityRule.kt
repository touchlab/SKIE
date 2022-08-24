package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import kotlin.reflect.KClass

internal interface AnnotationApplicabilityRule<IR> : ValidationRule<IR>
        where IR : IrElement, IR : IrAnnotationContainer {

    val targetAnnotation: KClass<out Annotation>

    override fun isSatisfied(element: IR): Boolean =
        !element.hasAnnotation(targetAnnotation) || isAnnotationApplicable(element)

    fun isAnnotationApplicable(element: IR): Boolean
}