package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import kotlin.reflect.KClass

internal class ConflictingAnnotationsRule<IR>(
    private val annotations: List<KClass<out Annotation>>,
) : ValidationRule<IR> where IR : IrElement, IR : IrAnnotationContainer {

    override val severity: CompilerMessageSeverity = CompilerMessageSeverity.ERROR

    override val message: String =
        "Annotations ${annotations.joinToString { "'${it.qualifiedName}'" }} cannot be used at the same time."

    constructor(vararg annotations: KClass<out Annotation>) : this(annotations.toList())

    override fun isSatisfied(element: IR): Boolean =
        annotations.count { element.hasAnnotation(it) } < 2
}