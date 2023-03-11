package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

data class SwiftGenericTypeUsageSirType(
    override val declaration: SwiftIrTypeParameterDeclaration,
) : SwiftNonNullReferenceSirType {

    override fun toSwiftPoetUsage(): TypeName = TypeVariableName(declaration.name)
}
