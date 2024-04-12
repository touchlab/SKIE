package co.touchlab.skie.kir.type

data class NullableReferenceKirType(
    val nonNullType: NonNullReferenceKirType,
    val isNullableResult: Boolean = false,
) : ReferenceKirType()
