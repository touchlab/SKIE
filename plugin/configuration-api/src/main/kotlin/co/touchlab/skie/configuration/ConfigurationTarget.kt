package co.touchlab.skie.configuration

import kotlin.reflect.KClass

interface ConfigurationTarget {

    val fqName: String

    fun <T : Annotation> hasAnnotation(kClass: KClass<T>): Boolean

    fun <T : Annotation> findAnnotation(kClass: KClass<T>): T?
}

inline fun <reified T : Annotation> ConfigurationTarget.hasAnnotation(): Boolean =
    this.hasAnnotation(T::class)

inline fun <reified T : Annotation> ConfigurationTarget.findAnnotation(): T? =
    this.findAnnotation(T::class)
