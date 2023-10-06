package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.type.SirType

// WIP 2 Verify that all wrappers for SirCallableDeclaration copy correctly all properties

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
}

val SirCallableDeclaration.identifierAfterVisibilityChanges: String
    get() = when (visibility) {
        SirVisibility.PublicButReplaced -> "__$identifier"
        // WIP Will not be needed once the type is removed from header
        SirVisibility.Removed -> "__Skie__Removed__$identifier"
        else -> identifier
    }
