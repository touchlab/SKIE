package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations

object SkieLambdaErrorSirType: SwiftNonNullReferenceSirType, SkieErrorSirType {

    override val declaration = BuiltinDeclarations.SKIE.SkieLambdaErrorType

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage() = declaration.internalName.toSwiftPoetName()

    override fun toString(): String = asString()
}
