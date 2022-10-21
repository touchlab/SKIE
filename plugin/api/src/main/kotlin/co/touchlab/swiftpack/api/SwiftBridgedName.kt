package co.touchlab.swiftpack.api

sealed interface SwiftBridgedName {
    data class Absolute(val name: String): SwiftBridgedName {
        override fun resolve(): String = name
    }
    data class Relative(val parent: SwiftTypeName, val childName: String): SwiftBridgedName {
        val typealiasName: String
            get() = "${parent.qualifiedName}__$childName"

        val typealiasValue: String
            get() = "${parent.qualifiedName}.$childName"

        override fun resolve(): String = typealiasName
    }

    fun resolve(): String

    companion object {
        operator fun invoke(parentOrNull: SwiftTypeName?, name: String): SwiftBridgedName {
            return if (parentOrNull == null) {
                Absolute(name)
            } else {
                Relative(parentOrNull, name)
            }
        }
    }
}
