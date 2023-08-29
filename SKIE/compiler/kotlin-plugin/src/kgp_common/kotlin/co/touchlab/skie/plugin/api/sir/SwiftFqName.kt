package co.touchlab.skie.plugin.api.sir

import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

sealed interface SwiftFqName {

    val root: SwiftFqName
    val name: String
    val components: List<SwiftFqName>

    fun nested(name: String): SwiftFqName

    fun nested(child: SwiftFqName): SwiftFqName

    fun asString(nestingSeparator: String = "."): String

    fun toSwiftPoetName(): DeclaredTypeName

    sealed interface Local : SwiftFqName {

        override val root: TopLevel
        override val components: List<Local>

        override fun nested(name: String): Nested = Nested(this, name)

        override fun nested(child: SwiftFqName): Local = child.components.fold(this) { acc, component ->
            acc.nested(component.name)
        }

        // TODO: Should this be the only place that knows about `qualifiedLocalTypeName`?
        override fun toSwiftPoetName(): DeclaredTypeName = DeclaredTypeName.qualifiedLocalTypeName(asString())

        data class TopLevel(
            override val name: String,
        ) : Local {

            override val root: TopLevel = this
            override val components: List<TopLevel> = listOf(this)

            override fun asString(nestingSeparator: String): String = name

            override fun toString(): String = asString()
        }

        data class Nested(
            val parent: Local,
            override val name: String,
        ) : Local {

            override val root: TopLevel = parent.root
            override val components: List<Local> = parent.components + this

            override fun asString(nestingSeparator: String): String = listOf(parent.asString(), name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }
    }

    sealed interface External : SwiftFqName {

        override val root: TopLevel
        override val components: List<External>

        override fun nested(name: String): Nested = Nested(this, name)

        override fun nested(child: SwiftFqName): External = child.components.fold(this) { acc, component ->
            acc.nested(component.name)
        }

        override fun toSwiftPoetName(): DeclaredTypeName = DeclaredTypeName.qualifiedTypeName(asString())

        data class TopLevel(
            val module: String,
            override val name: String,
        ) : External {

            override val root: TopLevel = this
            override val components: List<TopLevel> = listOf(this)

            override fun asString(nestingSeparator: String): String = listOf(module, name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }

        data class Nested(
            val parent: External,
            override val name: String,
        ) : External {

            override val root: TopLevel = parent.root
            override val components: List<External> = parent.components + this

            override fun asString(nestingSeparator: String): String = listOf(parent.asString(), name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }
    }
}
