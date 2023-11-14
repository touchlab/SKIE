package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel

sealed interface SirCallableDeclaration : SirDeclaration, SirElementWithModifiers, SirElementWithAttributes, SirDeclarationWithScope {

    /**
     * Used to derive other names.
     *
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    /**
     * Use `reference` in generated Swift code.
     *
     * Examples:
     * foo  (for properties)
     * foo (for functions without parameters)
     * foo(param1:)
     * __foo(param1:) (visibility == PublicButReplaced)
     */
    val reference: String

    /**
     * Use `name` in header, api notes and documentation.
     *
     * Examples:
     * foo  (for properties)
     * foo() (for functions without parameters)
     * foo(param1:)
     * __foo(param1:) (visibility == PublicButReplaced)
     */
    val name: String

    /**
     * Signature is not valid if and only if it references a SkieErrorType.
     * Only valid signatures can be used in generated Swift code.
     * Invalid signatures can be used only for generating placeholder declaration that cannot be called.
     * Example of such situation is if the signature contains a lambda type argument, such as A<() -> Unit>.
     */
    val hasValidSignature: Boolean

    val deprecationLevel: DeprecationLevel
}

val SirCallableDeclaration.receiverDeclaration: SirClass?
    get() = when (val parent = parent) {
        is SirClass -> parent
        is SirExtension -> parent.classDeclaration
        else -> null
    }

val SirCallableDeclaration.identifierAfterVisibilityChanges: String
    get() = when (visibility) {
        SirVisibility.PublicButReplaced -> "__$identifier"
        else -> identifier
    }
