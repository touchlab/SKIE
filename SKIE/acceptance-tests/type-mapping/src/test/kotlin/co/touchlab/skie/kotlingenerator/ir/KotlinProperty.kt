package co.touchlab.skie.kotlingenerator.ir

class KotlinProperty(
    val name: String,
    val type: KotlinType,
    val initializer: String,
) : KotlinDeclaration
