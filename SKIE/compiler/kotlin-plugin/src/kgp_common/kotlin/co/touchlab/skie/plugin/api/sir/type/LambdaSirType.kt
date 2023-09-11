package co.touchlab.skie.plugin.api.sir.type

import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeName

data class LambdaSirType(
    val returnType: SirType,
    val valueParameterTypes: List<SirType>,
    val isEscaping: Boolean,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isPrimitive: Boolean = false

    override val directlyReferencedTypes: List<SirType> = listOf(returnType) + valueParameterTypes

    override fun toSwiftPoetUsage(): TypeName = FunctionTypeName.get(
        parameters = valueParameterTypes.map { ParameterSpec.unnamed(it.toSwiftPoetUsage()) },
        returnType = returnType.toSwiftPoetUsage(),
        attributes = if (isEscaping) {
            listOf(AttributeSpec.ESCAPING)
        } else {
            emptyList()
        },
    )
}
