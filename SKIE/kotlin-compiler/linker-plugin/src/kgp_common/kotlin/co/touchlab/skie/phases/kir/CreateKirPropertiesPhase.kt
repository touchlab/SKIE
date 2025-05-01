@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.kir.util.addOverrides
import co.touchlab.skie.phases.KirPhase
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

internal class CreateKirPropertiesPhase(context: KirPhase.Context) : BaseCreateRegularKirMembersPhase(context, supportsProperties = true) {

    private val propertyCache = mutableMapOf<PropertyDescriptor, KirProperty>()

    override fun visitProperty(descriptor: PropertyDescriptor, kirClass: KirClass, origin: Origin) {
        getOrCreateProperty(descriptor, kirClass, origin)
    }

    private fun getOrCreateProperty(descriptor: PropertyDescriptor, kirClass: KirClass, origin: Origin): KirProperty =
        propertyCache.getOrPut(descriptor.original) {
            createProperty(descriptor, kirClass, origin)
        }

    private fun createProperty(descriptor: PropertyDescriptor, kirClass: KirClass, origin: Origin): KirProperty {
        val baseDescriptor = descriptor.baseProperty
        val originalDescriptor = descriptor.original

        val getterBridge = mapper.bridgeMethod(baseDescriptor.getter!!)

        val propertyName = namer.getPropertyName(baseDescriptor)

        kirClass.withTypeParameterScope {
            val property = KirProperty(
                kotlinName = descriptor.name.asString(),
                kotlinSignature = descriptor.signature,
                objCName = propertyName.objCName,
                swiftName = propertyName.swiftName,
                owner = kirClass,
                origin = origin,
                scope = kirClass.callableDeclarationScope,
                isFakeOverride = !descriptor.kind.isReal,
                type = kirDeclarationTypeTranslator.mapReturnType(originalDescriptor.getter!!, getterBridge.returnBridge),
                isVar = descriptor.setter?.let { mapper.shouldBeExposed(it) } ?: false,
                deprecationLevel = descriptor.kirDeprecationLevel,
                isRefinedInSwift = baseDescriptor.isRefinedInSwift,
                configuration = descriptorConfigurationProvider.getConfiguration(originalDescriptor),
                modality = descriptor.kirModality,
            )

            descriptorKirProvider.registerCallableDeclaration(property, descriptor)

            getDirectParents(descriptor)
                .map { getOrCreateOverriddenProperty(it, origin) }
                .let { property.addOverrides(it) }

            return property
        }
    }

    private fun getOrCreateOverriddenProperty(descriptor: PropertyDescriptor, origin: Origin): KirProperty {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported property $descriptor")

        val kirClass = descriptorKirProvider.getClass(classDescriptor)

        return getOrCreateProperty(descriptor, kirClass, origin)
    }
}
