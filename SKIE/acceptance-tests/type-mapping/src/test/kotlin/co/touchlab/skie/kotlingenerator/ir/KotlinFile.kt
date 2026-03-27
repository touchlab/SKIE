package co.touchlab.skie.kotlingenerator.ir

data class KotlinFile(
    val packageName: String,
    val declarations: List<KotlinDeclaration>,
)
