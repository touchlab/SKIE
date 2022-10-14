package co.touchlab.swiftlink.plugin.transform

sealed interface BridgedName {
    data class Absolute(val name: String): BridgedName {
        override fun resolve(): String = name
    }
    data class Relative(val parent: ResolvedName, val childName: String): BridgedName {
        val typealiasName: String
            get() = "${parent.newQualifiedName()}__$childName"

        val typealiasValue: String
            get() = "${parent.newQualifiedName()}.$childName"

        override fun resolve(): String = typealiasName
    }

    fun resolve(): String
}
