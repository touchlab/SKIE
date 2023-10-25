package co.touchlab.skie.phases.features.functions

import co.touchlab.skie.configuration.FunctionInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.phases.SirPhase

class FileScopeConvertor(
    context: SirPhase.Context,
) : SirPhase {

    private val parentProvider = FileScopeConversionParentProvider(context)

    private val globalMembersDelegate = GlobalMembersConvertorDelegate(parentProvider)
    private val interfaceExtensionMembersDelegate = InterfaceExtensionMembersConvertorDelegate(parentProvider)

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allClasses
            .filter { it.kind == KirClass.Kind.File }
            .flatMap { it.callableDeclarations }
            .filter { it.isInteropEnabled }
            .forEach {
                generateCallableDeclarationWrapper(it)
            }
    }

    context(SirPhase.Context)
    private val KirCallableDeclaration<*>.isInteropEnabled: Boolean
        get() = this.getConfiguration(FunctionInterop.FileScopeConversion.Enabled)

    private fun generateCallableDeclarationWrapper(callableDeclaration: KirCallableDeclaration<*>) {
        when (callableDeclaration) {
            is KirConstructor -> error("Constructors cannot be located in file class. Was: $callableDeclaration")
            is KirSimpleFunction -> generateFunctionWrapper(callableDeclaration)
            is KirProperty -> globalMembersDelegate.generateGlobalPropertyWrapper(callableDeclaration)
        }
    }

    private fun generateFunctionWrapper(function: KirSimpleFunction) {
        when (function.origin) {
            KirCallableDeclaration.Origin.Member -> error("Member functions shouldn't be in file classes. Was: $function")
            KirCallableDeclaration.Origin.Extension -> generateInterfaceExtensionWrapper(function)
            KirCallableDeclaration.Origin.Global -> globalMembersDelegate.generateGlobalFunctionWrapper(function)
        }
    }

    private fun generateInterfaceExtensionWrapper(function: KirSimpleFunction) {
        when (function.kind) {
            KirSimpleFunction.Kind.Function -> interfaceExtensionMembersDelegate.generateInterfaceExtensionFunctionWrapper(function)
            is KirSimpleFunction.Kind.PropertyGetter -> interfaceExtensionMembersDelegate.generateInterfaceExtensionPropertyWrapper(function)
            is KirSimpleFunction.Kind.PropertySetter -> {
                // Property wrapper must be generated only once
                if (function.kind.propertyDescriptor.getter == null) {
                    interfaceExtensionMembersDelegate.generateInterfaceExtensionPropertyWrapper(function)
                }
            }
        }
    }
}
