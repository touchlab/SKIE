package co.touchlab.skie.configuration

sealed interface ValueParameterConfigurationParent {

    val configuration: SkieConfiguration

    data class CallableDeclaration(override val configuration: CallableDeclarationConfiguration) : ValueParameterConfigurationParent

    data class Class(override val configuration: ClassConfiguration) : ValueParameterConfigurationParent
}
