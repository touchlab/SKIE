package co.touchlab.skie.kotlingenerator.ir

class KotlinClass(
    val name: String,
    val kind: Kind = Kind.Class,
    val typeParameters: List<KotlinTypeParameter> = emptyList(),
    val declarations: List<KotlinDeclaration> = emptyList(),
) : KotlinDeclaration {

    enum class Kind {
        Class, Interface
    }
}
