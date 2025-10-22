package co.touchlab.skie.configuration

import kotlin.reflect.KClass

interface ConfigurationTarget {

    fun hasAnnotation(kClass: KClass<out Annotation>): Boolean

    fun <T : Annotation> findAnnotation(kClass: KClass<T>): T?
}

inline fun <reified T : Annotation> ConfigurationTarget.hasAnnotation(): Boolean =
    this.hasAnnotation(T::class)

inline fun <reified T : Annotation> ConfigurationTarget.findAnnotation(): T? =
    this.findAnnotation(T::class)
