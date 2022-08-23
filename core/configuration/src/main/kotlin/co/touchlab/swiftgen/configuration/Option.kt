package co.touchlab.swiftgen.configuration

sealed interface Option<T> {

    val name: kotlin.String

    val description: kotlin.String

    val valueDescription: kotlin.String

    var value: T

    fun serialize(): kotlin.String

    fun deserialize(value: kotlin.String)

    data class String(
        override val name: kotlin.String,
        override val description: kotlin.String,
        override val valueDescription: kotlin.String,
        override var value: kotlin.String,
    ) : Option<kotlin.String> {

        override fun serialize(): kotlin.String = value

        override fun deserialize(value: kotlin.String) {
            this.value = value
        }
    }

    data class Boolean(
        override val name: kotlin.String,
        override val description: kotlin.String,
        override val valueDescription: kotlin.String,
        override var value: kotlin.Boolean,
    ) : Option<kotlin.Boolean> {

        override fun serialize(): kotlin.String =
            value.toString()

        override fun deserialize(value: kotlin.String) {
            this.value = value.toBooleanStrict()
        }
    }
}