package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.AnnotationValue
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

inline fun <reified T : Annotation> Annotated.hasAnnotation(): Boolean =
    hasAnnotation(T::class)

fun <T : Annotation> Annotated.hasAnnotation(annotation: KClass<T>): Boolean {
    val annotationName = FqName(annotation.qualifiedName!!)

    return this.annotations.hasAnnotation(annotationName)
}

inline fun <reified T : Annotation> Annotated.findAnnotation(): T? =
    findAnnotation(T::class)

fun <T : Annotation> Annotated.findAnnotation(annotationClass: KClass<T>): T? {
    val annotationName = FqName(annotationClass.qualifiedName!!)
    val annotation = this.annotations.findAnnotation(annotationName) ?: return null

    val constructor = annotationClass.constructors.first()

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

            parameter to argumentExpression.runtimeValue
        }
        .toMap()

private val ConstantValue<*>.runtimeValue: Any?
    get() = when (this) {
        is KClassValue, is EnumValue, is ArrayValue, is AnnotationValue -> {
            throw AssertionError("Unsupported annotation parameter type $this.")
        }

        else -> this.value
    }
