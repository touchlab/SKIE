package co.touchlab.skie.phases.features.functions

import co.touchlab.skie.kir.element.KirBridgeableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.receiverDeclaration
import io.outfoxx.swiftpoet.TypeName

interface FileScopeConvertorDelegateScope {

    fun <T : SirCallableDeclaration> configureBridge(
        callableDeclaration: KirBridgeableDeclaration<T>,
        originalSirCallableDeclaration: T,
        newSirCallableDeclaration: T,
    ) {
        // Replaces bridge only if there is no different bridged already present (for example for async functions)
        when (callableDeclaration.bridgedSirDeclaration) {
            originalSirCallableDeclaration, null -> callableDeclaration.bridgedSirDeclaration = newSirCallableDeclaration
            else -> {
            }
        }
    }

    fun KirClass.findFunctionWithKind(kind: KirSimpleFunction.Kind): KirSimpleFunction? =
        callableDeclarations.filterIsInstance<KirSimpleFunction>().firstOrNull { it.kind == kind }

    val SirCallableDeclaration.kotlinStaticMemberOwnerTypeName: TypeName
        get() {
            val owner = receiverDeclaration ?: error("Callable declarations from Kotlin should always have an owner. Was: $this")

            return owner.defaultType.evaluate().swiftPoetTypeName
        }
}
