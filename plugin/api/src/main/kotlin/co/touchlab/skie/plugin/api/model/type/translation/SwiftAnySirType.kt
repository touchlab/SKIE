package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.AnyTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftAnySirType: SwiftNonNullReferenceSirType {
    override val declaration = BuiltinDeclarations.Any

    override fun toSwiftPoetUsage(): TypeName = AnyTypeName.INSTANCE
}
