package co.touchlab.skie.sir.element

import org.intellij.lang.annotations.Language

sealed interface SirDeclaration : SirElement {

    val parent: SirDeclarationParent

    @set:Language("markdown")
    var documentation: String
}

val SirDeclaration.module: SirModule
    get() = parent.module

val SirDeclaration.firstParentThatIsNotNamespace: SirDeclarationParent
    get() = if (parent is SirDeclarationNamespace) parent.firstParentThatIsNotNamespace else parent

val SirDeclaration.topLevelParent: SirTopLevelDeclarationParent
    get() = (parent as? SirTopLevelDeclarationParent) ?: parent.topLevelParent
        ?: error("No top-level parent found for $this. All parent hierarchy should have a top-level parent.")

@get:JvmName("topLevelParentForCombinedDeclaration")
val <T> T.topLevelParent: SirTopLevelDeclarationParent where T : SirDeclaration, T : SirDeclarationParent
    get() = (this as SirDeclaration).topLevelParent

@Suppress("RecursivePropertyAccessor")
val SirDeclaration.file: SirFile?
    get() = parent as? SirFile ?: (parent as? SirDeclaration)?.file
