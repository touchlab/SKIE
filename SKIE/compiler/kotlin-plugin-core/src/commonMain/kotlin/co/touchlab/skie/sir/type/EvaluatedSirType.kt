package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

sealed interface EvaluatedSirType {

    val type: SirType

    // Always points to FqName. Used for signature matching.
    val canonicalName: String

    // Uses either FqName or internal name depending on context. Used for generating code.
    val swiftPoetTypeName: TypeName

    class Eager(
        override val type: SirType,
        override val canonicalName: String,
        override val swiftPoetTypeName: TypeName,
    ) : EvaluatedSirType

    class Lazy(
        typeProvider: kotlin.Lazy<SirType>,
        canonicalNameProvider: kotlin.Lazy<String>,
        swiftPoetTypeNameProvider: kotlin.Lazy<TypeName>,
    ) : EvaluatedSirType {

        override val type: SirType by typeProvider

        override val canonicalName: String by canonicalNameProvider

        override val swiftPoetTypeName: TypeName by swiftPoetTypeNameProvider
    }
}
