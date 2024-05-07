package co.touchlab.skie.kir.type

sealed class DeclarationBackedKirType : NonNullReferenceKirType() {

    abstract fun asDeclaredKirTypeOrError(): DeclaredKirType
}
