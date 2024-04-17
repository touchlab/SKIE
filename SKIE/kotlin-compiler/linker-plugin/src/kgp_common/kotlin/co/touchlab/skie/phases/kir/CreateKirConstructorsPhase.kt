@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.phases.KirCompilerPhase
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor

internal class CreateKirConstructorsPhase(
    context: KirCompilerPhase.Context,
) : BaseCreateRegularKirFunctionPhase(context, supportsConstructors = true) {

    override fun visitConstructor(descriptor: ConstructorDescriptor, kirClass: KirClass) {
        val originalDescriptor = descriptor.original

        val methodBridge = mapper.bridgeMethod(originalDescriptor)

        val constructor = KirConstructor(
            kotlinName = descriptor.name.asString(),
            kotlinSignature = descriptor.signature,
            objCSelector = namer.getSelector(descriptor),
            swiftName = namer.getSwiftName(descriptor),
            owner = kirClass,
            errorHandlingStrategy = methodBridge.returnBridge.errorHandlingStrategy,
            deprecationLevel = descriptor.kirDeprecationLevel,
            configuration = descriptorConfigurationProvider.getConfiguration(descriptor),
        )

        descriptorKirProvider.registerCallableDeclaration(constructor, descriptor)

        kirClass.withTypeParameterScope {
            createValueParameters(constructor, originalDescriptor, methodBridge)
        }
    }
}
