package co.touchlab.skie.phases.bridging

import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.CodeBlock

sealed interface CustomPassthroughDeclaration {
    data class Property(
        val identifier: String,
        val type: SirType,
        val visibility: SirVisibility = SirVisibility.Public,
        val scope: SirScope = SirScope.Member,
        val transformGetter: (CodeBlock) -> CodeBlock = { it },
        val setter: Setter? = null,
    ) : CustomPassthroughDeclaration {

        sealed interface Setter {
            object MutableProperty : Setter

            data class SimpleFunction(val identifier: String, val parameterLabel: String? = null) : Setter
        }
    }

    data class SimpleFunction(
        val identifier: String,
        val returnType: SirType,
        val visibility: SirVisibility = SirVisibility.Public,
        val scope: SirScope = SirScope.Member,
        val isAsync: Boolean = false,
        val throws: Boolean = false,
        val valueParameters: List<ValueParameter> = emptyList(),
        val transformBody: (CodeBlock) -> CodeBlock = { it },
    ) : CustomPassthroughDeclaration {

        data class ValueParameter(
            val label: String? = null,
            val name: String,
            val type: SirType,
            val transformAccess: (CodeBlock) -> CodeBlock = { it },
        )
    }
}
