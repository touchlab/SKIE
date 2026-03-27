package co.touchlab.skie.kotlingenerator.ir

class KotlinFunction(
    val name: String,
    val extensionReceiver: KotlinType? = null,
    val valueParameters: List<KotlinValueParameter>,
    val returnType: KotlinType,
    val body: String,
    val isSuspend: Boolean = false,
    val annotations: List<String> = emptyList(),
) : KotlinDeclaration
