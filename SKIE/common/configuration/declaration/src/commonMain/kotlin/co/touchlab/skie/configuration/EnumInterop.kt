package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.EnumInterop

object EnumInterop {

    /**
     * If true, the interop code is generated for the given enum.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override val skieRuntimeValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
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
     * ```
     *   case termsOfUse
     *   case privacyPolicy
     *   case none
     * ```
     */
    object LegacyCaseNames : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override val skieRuntimeValue: Boolean = false

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<EnumInterop.LegacyCaseNames>() -> true
                configurationTarget.hasAnnotation<EnumInterop.LegacyCaseNames>() -> false
                else -> null
            }
    }
}
