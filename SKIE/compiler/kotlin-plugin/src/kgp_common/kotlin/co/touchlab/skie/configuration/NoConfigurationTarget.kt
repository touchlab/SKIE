package co.touchlab.skie.configuration

import kotlin.reflect.KClass

object NoConfigurationTarget : ConfigurationTarget {

    override val belongsToSkieRuntime: Boolean = false

    override val fqName: String = ""

    override fun <T : Annotation> hasAnnotation(kClass: KClass<T>): Boolean = false

    override fun <T : Annotation> findAnnotation(kClass: KClass<T>): T? = null
}
