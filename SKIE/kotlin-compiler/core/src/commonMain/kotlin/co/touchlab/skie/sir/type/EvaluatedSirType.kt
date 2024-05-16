package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import io.outfoxx.swiftpoet.TypeName

sealed interface EvaluatedSirType {

    val type: SirType

    // Always points to FqName. Used for signature matching.
    val canonicalName: String

    // Uses either FqName or internal name depending on context. Used for generating code.
    val swiftPoetTypeName: TypeName

    val visibilityConstraint: SirVisibility

    val referencedTypeDeclarations: Set<SirTypeDeclaration>

    class Eager(
        override val type: SirType,
        override val canonicalName: String,
        override val swiftPoetTypeName: TypeName,
        override val visibilityConstraint: SirVisibility,
        override val referencedTypeDeclarations: Set<SirTypeDeclaration>,
    ) : EvaluatedSirType

    class Lazy(
        typeProvider: kotlin.Lazy<SirType>,
        canonicalNameProvider: kotlin.Lazy<String>,
        swiftPoetTypeNameProvider: kotlin.Lazy<TypeName>,
        lowestVisibility: kotlin.Lazy<SirVisibility>,
        referencedTypeDeclarationsProvider: kotlin.Lazy<Set<SirTypeDeclaration>>,
    ) : EvaluatedSirType {

        override val type: SirType by typeProvider

        override val canonicalName: String by canonicalNameProvider

        override val swiftPoetTypeName: TypeName by swiftPoetTypeNameProvider

        override val visibilityConstraint: SirVisibility by lowestVisibility

        override val referencedTypeDeclarations: Set<SirTypeDeclaration> by referencedTypeDeclarationsProvider
    }
}
