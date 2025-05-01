package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.util.BaseOverridableDeclaration
import co.touchlab.skie.kir.util.BaseOverridableDeclarationDelegate
import co.touchlab.skie.sir.element.SirCallableDeclaration

sealed interface KirOverridableDeclaration<T : KirOverridableDeclaration<T, S>, S : SirCallableDeclaration> :
    KirCallableDeclaration<S>,
    BaseOverridableDeclaration<T>

class KirOverridableDeclarationDelegate<T : KirOverridableDeclaration<T, S>, S : SirCallableDeclaration>(self: T) :
    BaseOverridableDeclarationDelegate<T>(self)
