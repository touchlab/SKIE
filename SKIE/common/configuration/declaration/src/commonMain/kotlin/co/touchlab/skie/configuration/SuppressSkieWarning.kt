package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.SuppressSkieWarning

object SuppressSkieWarning {

    /**
     * If true, SKIE will not raise a warning that it renamed the given declaration because of a name collision.
     */
    object NameCollision : ConfigurationKey.Boolean, ConfigurationScope.All {

        override val defaultValue: Boolean = false

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            configurationTarget.findAnnotation<SuppressSkieWarning.NameCollision>()?.suppress
    }
}
