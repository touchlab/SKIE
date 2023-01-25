---
title: Available Configurations
---

## Sealed classes/interfaces

```kotlin
object SealedInterop {

    /**
     * If true, the interop code is generated for the given sealed class/interface.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<SealedInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<SealedInterop.Disabled>() -> false
                else -> null
            }
    }

    object Function {

        /**
         * The name for the function used inside `switch`.
         */
        object Name : ConfigurationKey.String {

            override val defaultValue: String = "onEnum"

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.Name>()?.name
        }

        /**
         * The argument label for the function used inside `switch`.
         * Disable the argument label by passing "_".
         * No argumentLabel is generated if the name is empty.
         */
        object ArgumentLabel : ConfigurationKey.String {

            override val defaultValue: String = "of"

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.ArgumentLabel>()?.argumentLabel
        }

        /**
         * The parameter name for the function used inside `switch`.
         */
        object ParameterName : ConfigurationKey.String {

            override val defaultValue: String = "sealed"

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Function.ParameterName>()?.parameterName
        }
    }

    /**
     * The name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
     */
    object ElseName : ConfigurationKey.String {

        override val defaultValue: String = "Else"

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
            configurationTarget.findAnnotation<SealedInterop.ElseName>()?.elseName
    }

    object Case {

        /**
         * If false, given subclass will be hidden from the generated code, which means no dedicated enum case will be generated for it.
         */
        object Visible : ConfigurationKey.Boolean {

            override val defaultValue: Boolean = true

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
                when {
                    configurationTarget.hasAnnotation<SealedInterop.Case.Visible>() -> true
                    configurationTarget.hasAnnotation<SealedInterop.Case.Hidden>() -> false
                    else -> null
                }
        }

        /**
         * Overrides the name of the enum case generated for given subclass.
         */
        object Name : ConfigurationKey.OptionalString {

            override val defaultValue: String? = null

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): String? =
                configurationTarget.findAnnotation<SealedInterop.Case.Name>()?.name
        }
    }
}
```

## Exhaustive enums

```kotlin
object EnumInterop {

    /**
     * If true, the interop code is generated for the given enum.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<EnumInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<EnumInterop.Disabled>() -> false
                else -> null
            }
    }
}
```

## Default arguments/parameters

```kotlin
object DefaultArgumentInterop {

    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Disabled>() -> false
                else -> null
            }
    }

    object MaximumDefaultArgumentCount : ConfigurationKey.Int {

        override val defaultValue: Int = 5

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Int? =
            configurationTarget.findAnnotation<DefaultArgumentInterop.MaximumDefaultArgumentCount>()?.count
    }
}
```

## Experimental features

```kotlin
object ExperimentalFeatures {

    /**
     * If true, enables experimental features which might not be fully implemented yet which may cause compilation problems.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<ExperimentalFeatures.Enabled>() -> true
                configurationTarget.hasAnnotation<ExperimentalFeatures.Disabled>() -> false
                else -> null
            }
    }
}
```

## Validation

```kotlin
object Validation {

    /**
     * The severity of messages thrown by the compiler plugin as a result of validation error.
     */
    object Severity : ConfigurationKey<ValidationSeverity> {

        override val defaultValue: ValidationSeverity = ValidationSeverity.Error

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): ValidationSeverity? =
            null

        override fun deserialize(value: String?): ValidationSeverity =
            ValidationSeverity.valueOf(value.throwIfNull())
    }
}
```
