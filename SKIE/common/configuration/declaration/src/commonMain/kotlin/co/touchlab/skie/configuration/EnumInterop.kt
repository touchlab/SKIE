package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.EnumInterop

object EnumInterop {

    /**
     * If true, the interop code is generated for the given enum.
     */
    object Enabled : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = true

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<EnumInterop.Enabled>() -> true
            configurationTarget.hasAnnotation<EnumInterop.Disabled>() -> false
            else -> null
        }
    }

    /**
     * If enabled, SKIE uses the original Kotlin compiler algorithm for naming enum cases.
     *
     * The original algorithm supports UPPER_SNAKE_CASE but does not properly support PascalCase and camelCase.
     * For example:
     *
     * ```kotlin
     * enum class ModalDisplay {
     *     TermsOfUse,
     *     PrivacyPolicy,
     *     None
     * }
     * ```
     *
     * is translated to:
     *
     * ```swift
     *   case termsofuse
     *   case privacypolicy
     *   case none
     * ```
     *
     * SKIE provides an improved naming algorithm that produces the following:
     *
     * ```swift
     *   case termsOfUse
     *   case privacyPolicy
     *   case none
     * ```
     */
    object LegacyCaseName : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = false

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<EnumInterop.LegacyCaseName.Enabled>() -> true
            configurationTarget.hasAnnotation<EnumInterop.LegacyCaseName.Disabled>() -> false
            else -> null
        }
    }
}
