package co.touchlab.skie.phases.features.functions

import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.forEachAssociatedExportedSirDeclaration
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.call
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling

class GlobalMembersConvertorDelegate(
    private val parentProvider: FileScopeConversionParentProvider,
) : FileScopeConvertorDelegateScope {

    fun generateGlobalFunctionWrapper(function: KirSimpleFunction) {
        function.forEachAssociatedExportedSirDeclaration {
            generateGlobalFunctionWrapper(function, it)
        }
    }

    private fun generateGlobalFunctionWrapper(function: KirSimpleFunction, sirFunction: SirSimpleFunction) {
        parentProvider.forEachParent(function, sirFunction) {
            sirFunction.shallowCopy(parent = this).apply {
                copyValueParametersFrom(sirFunction)

                addFunctionBody(sirFunction)

                configureBridge(function, sirFunction, this)
            }
        }
    }

    private fun SirSimpleFunction.addFunctionBody(function: SirSimpleFunction) {
        addFunctionDeclarationBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L%T.%L",
                if (function.throws) "try " else "",
                if (function.isAsync) "await " else "",
                function.kotlinStaticMemberOwnerTypeName,
                function.call(valueParameters),
            )
        }
    }

    fun generateGlobalPropertyWrapper(property: KirProperty) {
        property.forEachAssociatedExportedSirDeclaration {
            generateGlobalPropertyWrapper(property, it)
        }
    }

    private fun generateGlobalPropertyWrapper(property: KirProperty, sirProperty: SirProperty) {
        parentProvider.forEachParent(property, sirProperty) {
            sirProperty.shallowCopy(parent = this).apply {
                sirProperty.getter?.let {
                    addPropertyGetter(it, sirProperty)
                }

                sirProperty.setter?.let {
                    addPropertySetter(it, sirProperty)
                }

                property.bridgedSirProperty = this
            }
        }
    }

    private fun SirProperty.addPropertyGetter(originalGetter: SirGetter, sirProperty: SirProperty) {
        SirGetter(
            attributes = originalGetter.attributes,
        ).apply {
            addFunctionDeclarationBodyWithErrorTypeHandling(sirProperty) {
                addStatement(
                    "return %T.%N",
                    sirProperty.kotlinStaticMemberOwnerTypeName,
                    sirProperty.reference,
                )
            }
        }
    }

    private fun SirProperty.addPropertySetter(originalSetter: SirSetter, sirProperty: SirProperty) {
        SirSetter(
            attributes = originalSetter.attributes,
            modifiers = originalSetter.modifiers,
        ).apply {
            addFunctionDeclarationBodyWithErrorTypeHandling(sirProperty) {
                addStatement(
                    "%T.%N = %N",
                    sirProperty.kotlinStaticMemberOwnerTypeName,
                    sirProperty.reference,
                    parameterName,
                )
            }
        }
    }
}
