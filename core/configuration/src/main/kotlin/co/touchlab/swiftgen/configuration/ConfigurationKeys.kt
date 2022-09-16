package co.touchlab.swiftgen.configuration

import co.touchlab.swiftgen.configuration.util.throwIfNull
import co.touchlab.swiftgen.configuration.values.ValidationSeverity
import co.touchlab.swiftgen.api.SealedInterop as SealedInteropAnnotations

object ConfigurationKeys {
    
    object Validation {

        object Severity : ConfigurationKey<ValidationSeverity> {

            override val name: String = "Validation.Severity"

            override val defaultValue: ValidationSeverity = ValidationSeverity.Error

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): ValidationSeverity? = null

            override fun deserialize(value: String?): ValidationSeverity =
                ValidationSeverity.valueOf(value.throwIfNull())
        }
    }

    object SealedInterop {

        /**
         * If true, the interop code is generated for the given sealed class/interface.
         */
        object Enabled : ConfigurationKey.Boolean {

            override val name: String = "SealedInterop.Enabled"

            override val defaultValue: Boolean = true

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
                when {
                    configurationTarget.hasAnnotation<SealedInteropAnnotations.Enabled>() -> true
                    configurationTarget.hasAnnotation<SealedInteropAnnotations.Disabled>() -> false
                    else -> null
                }
        }

        /**
         * The name for the function used inside `switch`.
         */
        object FunctionName : ConfigurationKey.String {

            override val name: String = "SealedInterop.FunctionName"

            override val defaultValue: String = "exhaustively"

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInteropAnnotations.FunctionName>()?.functionName
        }

        /**
         * The name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
         */
        object ElseName : ConfigurationKey.String {

            override val name: String = "SealedInterop.ElseName"

            override val defaultValue: String = "Else"

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInteropAnnotations.ElseName>()?.elseName
        }

        object Case {

            /**
             * If false, given subclass will be hidden from the generated code, which means no dedicated enum case will be generated for it.
             */
            object Visible : ConfigurationKey.Boolean {

                override val name: String = "SealedInterop.Case.Visible"

                override val defaultValue: Boolean = true

                override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
                    when {
                        configurationTarget.hasAnnotation<SealedInteropAnnotations.Case.Visible>() -> true
                        configurationTarget.hasAnnotation<SealedInteropAnnotations.Case.Hidden>() -> false
                        else -> null
                    }
            }

            /**
             * Overrides the name of the enum case generated for given subclass.
             */
            object Name : ConfigurationKey.OptionalString {

                override val name: String = "SealedInterop.Case.Name"

                override val defaultValue: String? = null

                override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                    configurationTarget.findAnnotation<SealedInteropAnnotations.Case.Name>()?.name
            }
        }
    }
}


