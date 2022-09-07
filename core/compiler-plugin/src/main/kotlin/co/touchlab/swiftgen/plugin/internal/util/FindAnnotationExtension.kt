package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.AnnotationValue
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal inline fun <reified T : Annotation> Annotated.hasAnnotation(): Boolean =
    hasAnnotation(T::class)

@Deprecated("Descriptors")
internal fun <T : Annotation> IrAnnotationContainer.hasAnnotation(annotation: KClass<T>): Boolean {
    val annotationName = FqName(annotation.qualifiedName!!)

    return this.hasAnnotation(annotationName)
}

internal fun <T : Annotation> Annotated.hasAnnotation(annotation: KClass<T>): Boolean {
    val annotationName = FqName(annotation.qualifiedName!!)

    return this.annotations.hasAnnotation(annotationName)
}

internal inline fun <reified T : Annotation> Annotated.findAnnotation(): T? {
    val annotationName = FqName(T::class.qualifiedName!!)
    val annotation = this.annotations.findAnnotation(annotationName) ?: return null

    val constructor = T::class.constructors.first()

    val parametersWithArguments = assignArgumentsToParameters(constructor.parameters, annotation)

    return constructor.callBy(parametersWithArguments)
}

private fun assignArgumentsToParameters(
    parameters: List<KParameter>,
    annotation: AnnotationDescriptor,
): Map<KParameter, Any?> =
    parameters
        .mapNotNull { parameter ->
            val argumentExpression = annotation.argumentValue(parameter.name!!) ?: return@mapNotNull null

            val argument = when (argumentExpression) {
                is KClassValue, is EnumValue, is ArrayValue, is AnnotationValue -> {
                    throw AssertionError("Unsupported annotation parameter type $argumentExpression.")
                }

                else -> argumentExpression.value
            }

            parameter to argument
        }
        .toMap()
