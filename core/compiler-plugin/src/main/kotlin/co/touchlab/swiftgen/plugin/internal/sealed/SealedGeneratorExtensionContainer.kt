package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.*
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal interface SealedGeneratorExtensionContainer {

    val configuration: SwiftGenConfiguration.SealedInteropDefaults

    val IrClass.elseCaseName: String
        get() = this.findAnnotation<SealedInterop.ElseName>()?.elseName ?: configuration.elseName

    val IrClassSymbol.enumCaseName: String
        get() {
            val annotation = this.owner.findAnnotation<SealedInterop.Case.Name>()

            return annotation?.name ?: this.owner.name.identifier
        }

    val IrClass.hasElseCase: Boolean
        get() = this.sealedSubclasses.any { !it.isVisibleSealedSubclass } || this.sealedSubclasses.isEmpty()

    val IrClass.visibleSealedSubclasses: List<IrClassSymbol>
        get() = this.sealedSubclasses.filter { it.isVisibleSealedSubclass }

    val IrClassSymbol.isVisibleSealedSubclass: Boolean
        get() {
            val isVisible = this.owner.isVisibleFromSwift

            val isEnabled = if (configuration.visibleCases) {
                !this.owner.hasAnnotation<SealedInterop.Case.Hidden>()
            } else {
                this.owner.hasAnnotation<SealedInterop.Case.Visible>()
            }

            return isVisible && isEnabled
        }

    fun IrClass.swiftNameWithTypeParametersForSealedCase(parent: IrClass): TypeName {
        if (this.isInterface) {
            return this.swiftName
        }

        val typeParameters = this.typeParameters.map {
            val indexInParent = it.indexInParent(parent)

            if (indexInParent != null) {
                parent.typeParameters[indexInParent].swiftName
            } else {
                TYPE_VARIABLE_BASE_BOUND_NAME
            }
        }

        return this.swiftName.withTypeParameters(typeParameters)
    }

    private fun IrTypeParameter.indexInParent(parent: IrClass): Int? {
        if (parent.isInterface) {
            return null
        }

        val parentType = this.parentAsClass.superTypes
            .firstOrNull { it.getClass() == parent }
            ?.cast<IrSimpleType>()
            ?: throw IllegalArgumentException("$parent is not a parent of $this.")

        val index = parentType.arguments.indexOfFirst { it.cast<IrSimpleType>().classifier.owner == this }

        return if (index != -1) index else null
    }
}