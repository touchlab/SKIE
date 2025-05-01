package co.touchlab.skie.phases.features.functions

import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.forEachAssociatedExportedSirDeclaration
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.isExported
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import co.touchlab.skie.util.swift.escapeSwiftIdentifier

class InterfaceExtensionMembersConvertorDelegate(private val parentProvider: FileScopeConversionParentProvider) :
    FileScopeConvertorDelegateScope {

    fun generateInterfaceExtensionFunctionWrapper(function: KirSimpleFunction) {
        function.forEachAssociatedExportedSirDeclaration {
            generateInterfaceExtensionFunctionWrapper(function, it)
        }
    }

    private fun generateInterfaceExtensionFunctionWrapper(function: KirSimpleFunction, sirFunction: SirSimpleFunction) {
        parentProvider.forEachParent(function, sirFunction) {
            sirFunction.shallowCopy(parent = this, scope = SirScope.Member).apply {
                copyValueParametersFrom(sirFunction.valueParameters.drop(1))

                addFunctionBody(sirFunction)

                configureBridge(function, sirFunction, this)
            }
        }

        sirFunction.isWrappedBySkie = true
    }

    private fun SirSimpleFunction.addFunctionBody(function: SirSimpleFunction) {
        addFunctionDeclarationBodyWithErrorTypeHandling(function) {
            addStatement(
                "return %L%L%T.%L",
                if (function.throws) "try " else "",
                if (function.isAsync) "await " else "",
                // Interfaces cannot be bridged
                function.kotlinStaticMemberOwnerTypeName,
                function.call(listOf("self") + valueParameters.map { it.name.escapeSwiftIdentifier() }),
            )
        }
    }

    fun generateInterfaceExtensionPropertyWrapper(function: KirSimpleFunction) {
        val (kirGetter, kirSetter) = when (val kind = function.kind) {
            is KirSimpleFunction.Kind.PropertyGetter -> function to kind.associatedSetter
            is KirSimpleFunction.Kind.PropertySetter -> kind.associatedGetter to function
            KirSimpleFunction.Kind.Function -> error("Function is not a converted property. Was: $function")
        }

        val getter = kirGetter?.originalSirFunction?.takeIf { it.isExported } ?: return
        val setter = kirSetter?.originalSirFunction?.takeIf { it.isExported }

        // Bridged converted properties are not supported at the moment as they are not easy to implement and not needed yet

        generateInterfaceExtensionPropertyWrapper(getter, setter, kirGetter)

        getter.isWrappedBySkie = true
        setter?.isWrappedBySkie = true
    }

    private fun generateInterfaceExtensionPropertyWrapper(
        getter: SirSimpleFunction,
        setter: SirSimpleFunction?,
        kirGetter: KirSimpleFunction,
    ) {
        parentProvider.forEachParent(kirGetter, getter) {
            SirProperty(
                identifier = getter.identifier,
                visibility = getter.visibility,
                type = getter.returnType,
                deprecationLevel = getter.deprecationLevel,
            ).apply {
                addPropertyGetter(getter)

                setter?.let {
                    addPropertySetter(setter)
                }
            }
        }
    }

    private fun SirProperty.addPropertyGetter(getter: SirSimpleFunction) {
        SirGetter(
            attributes = getter.attributes,
            throws = getter.throws,
        ).apply {
            addFunctionDeclarationBodyWithErrorTypeHandling(getter) {
                addStatement(
                    "return %T.%L",
                    getter.kotlinStaticMemberOwnerTypeName,
                    getter.call("self"),
                )
            }
        }
    }

    private fun SirProperty.addPropertySetter(setter: SirSimpleFunction) {
        SirSetter(
            attributes = setter.attributes,
            modifiers = setter.modifiers,
            throws = setter.throws,
        ).apply {
            addFunctionDeclarationBodyWithErrorTypeHandling(setter) {
                addStatement(
                    "%T.%L",
                    setter.kotlinStaticMemberOwnerTypeName,
                    setter.call("self", parameterName.escapeSwiftIdentifier()),
                )
            }
        }
    }
}
