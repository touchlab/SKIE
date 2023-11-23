package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel

sealed interface SirCallableDeclaration : SirDeclaration, SirElementWithModifiers, SirElementWithAttributes, SirDeclarationWithScope {

    /**
     * Used to derive other names.
     *
     * Examples:
     * foo
     * foo (visibility == PublicButReplaced)
     */
    val identifier: String

    /**
     * Used to derive other names.
     *
     * Examples:
     * foo
     * __foo (visibility == PublicButReplaced && !constructor)
     */
    val identifierAfterVisibilityChange: String

    /**
     * Use to obtain declaration `reference` in generated Swift code.
     *
     * Examples:
     * foo  (for properties)
     * foo (for functions without parameters)
     * foo(param1:)
     * __foo(param1:) (visibility == PublicButReplaced && !constructor)
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

    val deprecationLevel: DeprecationLevel
}

val SirCallableDeclaration.receiverDeclaration: SirClass?
    get() = when (val parent = parent) {
        is SirClass -> parent
        is SirExtension -> parent.classDeclaration
        else -> null
    }
