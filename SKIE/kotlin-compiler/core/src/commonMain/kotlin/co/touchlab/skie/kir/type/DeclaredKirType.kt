package co.touchlab.skie.kir.type

import co.touchlab.skie.kir.element.KirClass

data class DeclaredKirType(
    val declaration: KirClass,
    val typeArguments: List<KirType>,
) : DeclarationBackedKirType() {

    override fun asDeclaredKirTypeOrError(): DeclaredKirType = this
}
