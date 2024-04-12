package co.touchlab.skie.kir.type

data class BlockPointerKirType(
    val valueParameterTypes: List<KirType>,
    val returnType: KirType,
) : NonNullReferenceKirType()
