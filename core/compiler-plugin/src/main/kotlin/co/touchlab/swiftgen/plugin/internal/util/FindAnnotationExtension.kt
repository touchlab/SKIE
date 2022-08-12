package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KParameter

internal inline fun <reified T : Annotation> IrAnnotationContainer.findAnnotation(): T? {
    val annotationName = FqName(T::class.qualifiedName!!)
    val annotation = getAnnotation(annotationName) ?: return null

    val constructor = T::class.constructors.first()

    val parametersWithArguments = assignArgumentsToParameters(constructor.parameters, annotation)

    return constructor.callBy(parametersWithArguments)
}

private fun assignArgumentsToParameters(
    parameters: List<KParameter>,
    annotation: IrConstructorCall,
): Map<KParameter, Any?> =
    parameters
        .mapNotNull { parameter ->
            val argumentName = Name.identifier(parameter.name!!)

            val argumentExpression = annotation.getValueArgument(argumentName) ?: return@mapNotNull null

            val argument = when (argumentExpression) {
                is IrConst<*> -> argumentExpression.value
                /*
                     * IrClassReference is non-trivial to implement due to a limitation of Kotlin reflection.
                     * The following is not possible:
                     * is IrClassReference -> Class.forName(argumentExpression.classType.classFqName?.asString()).kotlin
                     * https://youtrack.jetbrains.com/issue/KT-10440/Add-Kotlin-equivalent-to-ClassforName-for-KClass
                     * Possible to workaround is to "mock" the created KClass.
                     */
                // Add support for other cases as necessary
                else -> throw AssertionError("Unsupported annotation parameter type $argumentExpression.")
            }

            parameter to argument
        }
        .toMap()
