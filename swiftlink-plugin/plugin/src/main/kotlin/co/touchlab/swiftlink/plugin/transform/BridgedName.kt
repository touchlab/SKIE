package co.touchlab.swiftlink.plugin.transform

sealed interface BridgedName {
    data class Absolute(val name: String): BridgedName {
        override fun resolve(): String = name
    }
    data class Relative(val parent: ResolvedName, val childName: String): BridgedName {
        override fun resolve(): String = "${parent.newQualifiedName()}.$childName"
    }

    fun resolve(): String
}
