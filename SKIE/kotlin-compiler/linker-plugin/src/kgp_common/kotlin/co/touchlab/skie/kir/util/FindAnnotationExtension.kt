package co.touchlab.skie.kir.util

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.AnnotationValue
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue

inline fun <reified T : Annotation> Annotated.hasAnnotation(): Boolean = hasAnnotation(T::class)

fun <T : Annotation> Annotated.hasAnnotation(annotation: KClass<T>): Boolean =
    this.annotations.any { it.fqName?.asString() == annotation.qualifiedName }

inline fun <reified T : Annotation> Annotated.findAnnotation(): T? = findAnnotation(T::class)

fun <T : Annotation> Annotated.findAnnotation(annotationClass: KClass<T>): T? {
    val annotation = this.annotations.firstOrNull { it.fqName?.asString() == annotationClass.qualifiedName } ?: return null

    val constructor = annotationClass.constructors.first()

    val parametersWithArguments = assignArgumentsToParameters(constructor.parameters, annotation)

    return constructor.callBy(parametersWithArguments)
}

private fun assignArgumentsToParameters(parameters: List<KParameter>, annotation: AnnotationDescriptor): Map<KParameter, Any?> = parameters
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

fun AnnotationDescriptor.hasArgumentValue(parameterName: String): Boolean = Name.identifier(parameterName) in this.allValueArguments
