package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.SealedInterop

object SealedInterop {

    /**
     * If true, the interop code is generated for the given sealed class/interface.
     */
    object Enabled : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = true

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<SealedInterop.Enabled>() -> true
            configurationTarget.hasAnnotation<SealedInterop.Disabled>() -> false
            else -> null
        }
    }

    /**
     * If true, SKIE exports all sealed children of this class/interface to Obj-C even if they wouldn't be exported by Kotlin compiler.
     * Otherwise, SKIE does not modify the default behavior.
     */
    object ExportEntireHierarchy : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = true

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<SealedInterop.EntireHierarchyExport.Enabled>() -> true
            configurationTarget.hasAnnotation<SealedInterop.EntireHierarchyExport.Disabled>() -> false
            else -> null
        }
    }

    object Function {

        /**
         * The name for the function used inside `switch`.
         */
        object Name : ConfigurationKey.String, ConfigurationScope.AllExceptCallableDeclarations {

            override val defaultValue: String = "onEnum"

            override fun findAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.Name>()?.name
        }

        /**
         * The argument label for the function used inside `switch`.
         * Disable the argument label by passing "_".
         * No argumentLabel is generated if the name is empty.
         */
        object ArgumentLabel : ConfigurationKey.String, ConfigurationScope.AllExceptCallableDeclarations {

            override val defaultValue: String = "of"

            override fun findAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.ArgumentLabel>()?.argumentLabel
        }

        /**
         * The parameter name for the function used inside `switch`.
         */
        object ParameterName : ConfigurationKey.String, ConfigurationScope.AllExceptCallableDeclarations {

            override val defaultValue: String = "sealed"

            override fun findAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.ParameterName>()?.parameterName
        }
    }

    /**
     * The name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
     */
    object ElseName : ConfigurationKey.String, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: String = "else"

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): String? =
            configurationTarget.findAnnotation<SealedInterop.ElseName>()?.elseName
    }

    object Case {

        /**
         * If false, given subclass will be hidden from the generated code, which means no dedicated enum case will be generated for it.
         */
        object Visible : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

            override val defaultValue: Boolean = true

            override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
                configurationTarget.hasAnnotation<SealedInterop.Case.Visible>() -> true
                configurationTarget.hasAnnotation<SealedInterop.Case.Hidden>() -> false
                else -> null
            }
        }

        /**
         * Overrides the name of the enum case generated for given subclass.
         */
        object Name : ConfigurationKey.OptionalString, ConfigurationScope.AllExceptCallableDeclarations {

            override val defaultValue: String? = null

            override fun hasAnnotationValue(configurationTarget: ConfigurationTarget): Boolean =
                configurationTarget.findAnnotation<SealedInterop.Case.Name>() != null

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Case.Name>()?.name
        }
    }
}
